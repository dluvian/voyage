package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.cmd.HomeViewAction
import com.dluvian.voyage.cmd.HomeViewAppend
import com.dluvian.voyage.cmd.HomeViewApplyFilter
import com.dluvian.voyage.cmd.HomeViewDismissFilter
import com.dluvian.voyage.cmd.HomeViewOpenFilter
import com.dluvian.voyage.cmd.HomeViewRefresh
import com.dluvian.voyage.cmd.HomeViewSubAccountAndTrustData
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.PostDetails
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.preferences.HomePreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


class HomeViewModel(
    feedProvider: FeedProvider,
    val postDetails: State<PostDetails?>,
    val feedState: LazyListState,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val homePreferences: HomePreferences,
) : ViewModel() {
    val showFilterMenu: MutableState<Boolean> = mutableStateOf(false)
    val setting: MutableState<HomeFeedSetting> =
        mutableStateOf(homePreferences.getHomeFeedSetting())
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = lazyNostrSubscriber.subCreator
    )

    init {
        paginator.init(setting = setting.value)
    }

    fun handle(action: HomeViewAction) {
        when (action) {
            HomeViewRefresh -> refresh()
            HomeViewAppend -> paginator.nextPage()
            HomeViewSubAccountAndTrustData -> subMyAccountAndTrustData()
            HomeViewOpenFilter -> showFilterMenu.value = true
            HomeViewDismissFilter -> showFilterMenu.value = false

            is HomeViewApplyFilter -> if (setting.value != action.setting) {
                homePreferences.setHomeFeedSettings(setting = action.setting)
                showFilterMenu.value = false
                setting.value = action.setting
                paginator.reinit(setting = action.setting, showRefreshIndicator = true)
            }
        }
    }

    private var job: Job? = null
    private fun subMyAccountAndTrustData() {
        if (job?.isActive == true) return
        job = viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubMyMainView()
            lazyNostrSubscriber.lazySubMyAccountAndTrustData()
            delay(DELAY_10SEC)
        }
    }

    private fun refresh() {
        lazyNostrSubscriber.subCreator.unsubAll()
        paginator.refresh(onSub = { subMyAccountAndTrustData() })
    }
}
