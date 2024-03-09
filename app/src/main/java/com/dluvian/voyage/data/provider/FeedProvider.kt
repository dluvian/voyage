package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.RootPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach

class FeedProvider {
    fun getFeedFlow(until: Long, size: Int, isRefresh: Boolean): Flow<List<RootPost>> {
        // TODO: Subscribe feed
        // TODO: Subscribe votes
        // TODO: Subscribe replies
        return emptyFlow<List<RootPost>>().onEach { }
    }
}
