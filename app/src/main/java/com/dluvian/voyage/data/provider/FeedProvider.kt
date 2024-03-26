package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
) {
    @OptIn(FlowPreview::class)
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
            posts.map { RootPostUI.from(it) }
                .map {
                    val vote = votes.getOrDefault(it.id, null)
                    if (vote != null) it.copy(myVote = vote) else it
                }
        }
            .debounce(SHORT_DEBOUNCE)
            .onEach { posts -> nostrSubscriber.subVotesAndReplies(postIds = posts.map { it.id }) }
    }
}
