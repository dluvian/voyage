package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.ReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.RootFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val room: AppDatabase,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val oldestUsedEvent: OldestUsedEvent,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val forcedFollows: Flow<Map<PubkeyHex, Boolean>>
) {
    suspend fun getFeedFlow(
        until: Long,
        subUntil: Long,
        size: Int,
        setting: FeedSetting,
        subscribe: Boolean = true,
    ): Flow<List<ParentUI>> {
        if (subscribe) {
            nostrSubscriber.subFeed(until = subUntil, limit = size, setting = setting)
        }

        return when (setting) {
            is RootFeedSetting -> getRootFeedFlow(
                until = until,
                size = size,
                setting = setting
            )

            is ReplyFeedSetting -> getReplyFeedFlow(until = until, size = size, setting = setting)

            InboxFeedSetting -> getInboxFeedFlow(until = until, size = size)
        }
            .firstThenDistinctDebounce(SHORT_DEBOUNCE)
            .onEach { posts ->
                oldestUsedEvent.updateOldestCreatedAt(posts.minOfOrNull { it.createdAt })
                nostrSubscriber.subVotesAndReplies(parentIds = posts.map { it.getRelevantId() })
            }
    }

    private fun getRootFeedFlow(
        until: Long,
        size: Int,
        setting: RootFeedSetting,
    ): Flow<List<RootPostUI>> {
        val flow = when (setting) {
            is HomeFeedSetting -> room.rootPostDao().getHomeRootPostFlow(until = until, size = size)
            is TopicFeedSetting -> room.rootPostDao().getTopicRootPostFlow(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPostFlow(
                pubkey = setting.pubkey,
                until = until,
                size = size
            )
        }

        return combine(flow, forcedVotes, forcedFollows) { posts, votes, follows ->
            posts.map {
                it.mapToRootPostUI(
                    forcedVotes = votes,
                    forcedFollows = follows,
                    annotatedStringProvider = annotatedStringProvider
                )
            }
        }
    }

    private fun getReplyFeedFlow(
        until: Long,
        size: Int,
        setting: ReplyFeedSetting,
    ): Flow<List<ReplyUI>> {
        val flow = room.replyDao().getProfileReplyFlow(
            pubkey = setting.pubkey,
            until = until,
            size = size
        )

        return combine(flow, forcedVotes, forcedFollows) { posts, votes, follows ->
            posts.map {
                it.mapToReplyUI(
                    forcedVotes = votes,
                    forcedFollows = follows,
                    annotatedStringProvider = annotatedStringProvider
                )
            }
        }
    }

    private fun getInboxFeedFlow(until: Long, size: Int): Flow<List<ParentUI>> {
        return combine(
            room.directReplyDao()
                .getDirectReplyFlow(until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            room.directCrossPostDao()
                .getDirectCrossPostFlow(until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows
        ) { replies, crossPosts, votes, follows ->
            mergeToParentUIList(
                replies = replies,
                crossPosts = crossPosts,
                votes = votes,
                follows = follows,
                size = size
            )
        }
    }

    suspend fun getStaticFeed(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): List<ParentUI> {
        return when (setting) {
            is RootFeedSetting -> getStaticRootFeed(until = until, size = size, setting = setting)
            is ReplyFeedSetting -> getStaticReplyFeed(until = until, size = size, setting = setting)
            InboxFeedSetting -> getStaticInboxFeed(until = until, size = size)
        }
    }

    private suspend fun getStaticRootFeed(
        until: Long,
        size: Int,
        setting: RootFeedSetting,
    ): List<RootPostUI> {
        return when (setting) {
            is HomeFeedSetting -> room.rootPostDao().getHomeRootPosts(until = until, size = size)
            is TopicFeedSetting -> room.rootPostDao().getTopicRootPosts(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPosts(
                pubkey = setting.pubkey,
                until = until,
                size = size
            )
        }.map {
            it.mapToRootPostUI(
                forcedVotes = emptyMap(),
                forcedFollows = emptyMap(),
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }

    private suspend fun getStaticReplyFeed(
        until: Long,
        size: Int,
        setting: ReplyFeedSetting,
    ): List<ReplyUI> {
        return room.replyDao().getProfileReplies(
            pubkey = setting.pubkey,
            until = until,
            size = size
        )
            .map {
                it.mapToReplyUI(
                    forcedVotes = emptyMap(),
                    forcedFollows = emptyMap(),
                    annotatedStringProvider = annotatedStringProvider
                )
            }
    }

    private suspend fun getStaticInboxFeed(until: Long, size: Int): List<ParentUI> {
        val replies = room.directReplyDao().getDirectReplies(until = until, size = size)
        val crossPosts = room.directCrossPostDao().getDirectCrossPosts(until = until, size = size)

        return mergeToParentUIList(
            replies = replies,
            crossPosts = crossPosts,
            votes = emptyMap(),
            follows = emptyMap(),
            size = size
        )
    }

    fun settingHasPostsFlow(setting: FeedSetting): Flow<Boolean> {
        return when (setting) {
            is HomeFeedSetting -> room.rootPostDao().hasHomeRootPostsFlow()
            is TopicFeedSetting -> room.rootPostDao().hasTopicRootPostsFlow(topic = setting.topic)
            is ProfileRootFeedSetting -> room.rootPostDao()
                .hasProfileRootPostsFlow(pubkey = setting.pubkey)

            is ReplyFeedSetting -> room.replyDao().hasProfileRepliesFlow(pubkey = setting.pubkey)

            InboxFeedSetting -> combine(
                room.directReplyDao().hasDirectRepliesFlow(),
                room.directCrossPostDao().hasDirectCrossPostsFlow()
            ) { hasReplies, hasCrossPosts ->
                hasReplies || hasCrossPosts
            }
        }
    }

    private fun mergeToParentUIList(
        replies: Collection<ReplyView>,
        crossPosts: Collection<RootPostView>,
        votes: Map<EventIdHex, Vote>,
        follows: Map<PubkeyHex, Boolean>,
        size: Int
    ): List<ParentUI> {
        val applicableTimestamps = replies.map { it.createdAt }
            .plus(crossPosts.map { it.createdAt })
            .sortedDescending()
            .take(size)
            .toSet()

        val result = mutableListOf<ParentUI>()
        for (reply in replies) {
            if (!applicableTimestamps.contains(reply.createdAt)) continue
            val mapped = reply.mapToReplyUI(
                forcedVotes = votes,
                forcedFollows = follows,
                annotatedStringProvider = annotatedStringProvider
            )
            result.add(mapped)
        }
        for (crossPost in crossPosts) {
            if (!applicableTimestamps.contains(crossPost.createdAt)) continue
            val mapped = crossPost.mapToRootPostUI(
                forcedVotes = votes,
                forcedFollows = follows,
                annotatedStringProvider = annotatedStringProvider
            )
            result.add(mapped)
        }
        return result.sortedByDescending { it.createdAt }.take(size)
    }
}
