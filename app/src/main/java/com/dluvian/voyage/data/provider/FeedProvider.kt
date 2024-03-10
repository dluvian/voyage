package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val postVoter: PostVoter,
) {
    fun getFeedFlow(
        until: Long,
        size: Int,
    ): Flow<List<RootPost>> {
        nostrSubscriber.subFeed(until = until, size = size)
        return rootPostDao.getRootPostFlow(until = until, size = size)
            .combine(postVoter.forcedVotes) { posts, votes ->
                posts.map { RootPost.from(it) }
                    .map {
                        val vote = votes.getOrDefault(it.id, null)
                        if (vote != null) it.copy(myVote = vote) else it
                    }
            }
            .onEach { posts -> nostrSubscriber.subVotesAndReplies(postIds = posts.map { it.id }) }
    }
}
