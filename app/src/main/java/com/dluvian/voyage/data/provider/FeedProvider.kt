package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao
) {
    fun getFeedFlow(
        until: Long,
        size: Int,
    ): Flow<List<RootPost>> {
        nostrSubscriber.subFeed(until = until, size = size)
        return rootPostDao.getRootPostFlow(until = until, size = size)
            .map { list -> list.map { RootPost.from(it) } }
            .onEach { posts -> nostrSubscriber.subVotesAndReplies(postIds = posts.map { it.id }) }
    }
}
