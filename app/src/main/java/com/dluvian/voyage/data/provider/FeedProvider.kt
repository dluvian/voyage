package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val oldestUsedEvent: OldestUsedEvent,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val nameCache: MutableMap<PubkeyHex, String?>,
) {
    suspend fun getFeedFlow(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): Flow<List<RootPostUI>> {
        nostrSubscriber.subFeed(until = until, limit = size, setting = setting)

        val flow = when (setting) {
            is HomeFeedSetting -> rootPostDao.getHomeRootPostFlow(until = until, size = size)
            is TopicFeedSetting -> rootPostDao.getTopicRootPostFlow(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileFeedSetting -> rootPostDao.getProfileRootPostFlow(
                pubkey = setting.pubkey,
                until = until,
                size = size
            )
        }

        return flow.combine(forcedVotes) { posts, votes ->
            posts.map {
                it.mapToRootPostUI(
                    forcedVotes = votes,
                    annotatedStringProvider = annotatedStringProvider
                )
            }
        }
            .firstThenDistinctDebounce(SHORT_DEBOUNCE)
            .onEach { posts ->
                oldestUsedEvent.updateOldestCreatedAt(posts.minOfOrNull { it.createdAt })
                nostrSubscriber.subVotesAndReplies(postIds = posts.map { it.id })
                posts.filter { it.authorName.isNotEmpty() }
                    .forEach { nameCache.putIfAbsent(it.pubkey, it.authorName) }
            }
    }

    suspend fun getStaticFeed(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): List<RootPostUI> {
        val posts = when (setting) {
            is HomeFeedSetting -> rootPostDao.getHomeRootPosts(until = until, size = size)
            is TopicFeedSetting -> rootPostDao.getTopicRootPosts(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileFeedSetting -> rootPostDao.getProfileRootPosts(
                pubkey = setting.pubkey,
                until = until,
                size = size
            )
        }

        return posts.map {
            it.mapToRootPostUI(
                forcedVotes = emptyMap(),
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }
}
