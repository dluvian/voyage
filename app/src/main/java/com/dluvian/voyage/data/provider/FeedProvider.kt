package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.mergeToParentUIList
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.ReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.RootFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.AppDatabase
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
    private val staticFeedProvider = StaticFeedProvider(
        room = room,
        annotatedStringProvider = annotatedStringProvider
    )

    suspend fun getStaticFeed(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): List<ParentUI> {
        return staticFeedProvider.getStaticFeed(
            until = until,
            size = size,
            setting = setting
        )
    }

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

            BookmarksFeedSetting -> getBookmarksFeedFlow(until = until, size = size)
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

        return combine(
            flow.firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows
        ) { posts, votes, follows ->
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

        return combine(
            flow.firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows
        ) { posts, votes, follows ->
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
                roots = crossPosts,
                votes = votes,
                follows = follows,
                size = size,
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }

    private fun getBookmarksFeedFlow(until: Long, size: Int): Flow<List<ParentUI>> {
        return combine(
            room.bookmarkDao()
                .getReplyFlow(until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            room.bookmarkDao()
                .getRootPostsFlow(until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows
        ) { replies, rootPosts, votes, follows ->
            mergeToParentUIList(
                replies = replies,
                roots = rootPosts,
                votes = votes,
                follows = follows,
                size = size,
                annotatedStringProvider = annotatedStringProvider
            )
        }
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

            BookmarksFeedSetting -> room.bookmarkDao().hasBookmarkedPostsFlow()
        }
    }
}
