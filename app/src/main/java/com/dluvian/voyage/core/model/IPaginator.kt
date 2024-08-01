package com.dluvian.voyage.core.model

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

interface IPaginator {
    val isInitialized: State<Boolean>
    val isRefreshing: State<Boolean>
    val isAppending: State<Boolean>
    val hasMoreRecentPosts: State<Boolean>
    val hasPosts: State<StateFlow<Boolean>>
    val page: State<StateFlow<List<ParentUI>>>
    val filteredPage: State<StateFlow<List<ParentUI>>>
}
