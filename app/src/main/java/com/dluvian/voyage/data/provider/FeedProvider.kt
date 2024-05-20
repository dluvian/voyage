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
import com.dluvian.voyage.data.model.ProfileReplyFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.RootFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.ReplyDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val replyDao: ReplyDao,
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
    ): Flow<List<ParentUI>> {
        // Sub only in getRootFeedFlow. Replies are a byproduct

        return when (setting) {
            is RootFeedSetting -> getRootFeedFlow(
                until = until,
                subUntil = subUntil,
                size = size,
                setting = setting
            )

            is ReplyFeedSetting -> getReplyFeedFlow(until = until, size = size, setting = setting)
        }
            .firstThenDistinctDebounce(SHORT_DEBOUNCE)
            .onEach { posts ->
                oldestUsedEvent.updateOldestCreatedAt(posts.minOfOrNull { it.createdAt })
                nostrSubscriber.subVotesAndReplies(parentIds = posts.map { it.getRelevantId() })
            }
    }

    private suspend fun getRootFeedFlow(
        until: Long,
        subUntil: Long,
        size: Int,
        setting: RootFeedSetting,
    ): Flow<List<RootPostUI>> {
        nostrSubscriber.subFeed(until = subUntil, limit = size, setting = setting)

        val flow = when (setting) {
            is HomeFeedSetting -> rootPostDao.getHomeRootPostFlow(until = until, size = size)
            is TopicFeedSetting -> rootPostDao.getTopicRootPostFlow(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileRootFeedSetting -> rootPostDao.getProfileRootPostFlow(
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
        // Replies are a byproduct of fetching root posts. Don't subscribe it again

        val flow = when (setting) {
            is ProfileReplyFeedSetting -> replyDao.getProfileReplyFlow(
                pubkey = setting.pubkey,
                until = until,
                size = size
            )
        }

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

    suspend fun getStaticFeed(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): List<ParentUI> {
        return when (setting) {
            is RootFeedSetting -> getStaticRootFeed(until = until, size = size, setting = setting)
            is ReplyFeedSetting -> getStaticReplyFeed(until = until, size = size, setting = setting)
        }
    }

    private suspend fun getStaticRootFeed(
        until: Long,
        size: Int,
        setting: RootFeedSetting,
    ): List<RootPostUI> {
        return when (setting) {
            is HomeFeedSetting -> rootPostDao.getHomeRootPosts(until = until, size = size)
            is TopicFeedSetting -> rootPostDao.getTopicRootPosts(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileRootFeedSetting -> rootPostDao.getProfileRootPosts(
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
        return when (setting) {
            is ProfileReplyFeedSetting -> replyDao.getProfileReplies(
                pubkey = setting.pubkey,
                until = until,
                size = size
            )
        }.map {
            it.mapToReplyUI(
                forcedVotes = emptyMap(),
                forcedFollows = emptyMap(),
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }

    fun settingHasPostsFlow(setting: FeedSetting): Flow<Boolean> {
        return when (setting) {
            is HomeFeedSetting -> rootPostDao.hasHomeRootPostsFlow()
            is TopicFeedSetting -> rootPostDao.hasTopicRootPostsFlow(topic = setting.topic)
            is ProfileRootFeedSetting -> rootPostDao.hasProfileRootPostsFlow(pubkey = setting.pubkey)
            is ProfileReplyFeedSetting -> replyDao.hasProfileRepliesFlow(pubkey = setting.pubkey)
        }
    }
}
