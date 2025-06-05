package com.dluvian.voyage.paginator

import androidx.compose.runtime.State
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.provider.IEventUpdate

interface IPaginator : IEventUpdate {
    val isRefreshing: State<Boolean>
    val isSwitchingPage: State<Boolean>
    val isNotFirstPage: State<Boolean>
    val showNextPageBtn: State<Boolean>
    val page: State<List<UIEvent>>

    fun initSetting(setting: FeedSetting)
    suspend fun refresh()
    suspend fun dbRefreshInPlace()
    suspend fun nextPage()
}
