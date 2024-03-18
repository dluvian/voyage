package com.dluvian.voyage.core.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

interface IPaginator {
    val isRefreshing: State<Boolean>
    val isAppending: State<Boolean>
    val page: MutableState<StateFlow<List<RootPost>>>
}
