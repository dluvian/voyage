package com.dluvian.voyage.data

import com.dluvian.voyage.core.model.RootPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach

private const val PAGE_SIZE = 25

class FeedProvider {
    fun getFeedFlow(page: Int): Flow<List<RootPost>> {
        return emptyFlow<List<RootPost>>().onEach { }
    }
}