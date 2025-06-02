package com.dluvian.voyage.paginator

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.model.UIEvent

interface IPaginator {
    val isRefreshing: State<Boolean>
    val isSwitchingPage: State<Boolean>
    val isNotFirstPage: State<Boolean>
    val page: MutableState<List<UIEvent>>
    suspend fun load(setting: FeedSetting)
    suspend fun refresh()
    suspend fun dbRefreshInPlace()
    suspend fun nextPage()
}
