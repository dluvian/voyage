package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.HomeViewAction
import com.dluvian.voyage.core.HomeViewAppend
import com.dluvian.voyage.core.HomeViewApplyFilter
import com.dluvian.voyage.core.HomeViewDismissFilter
import com.dluvian.voyage.core.HomeViewOpenFilter
import com.dluvian.voyage.core.HomeViewRefresh
import com.dluvian.voyage.core.HomeViewSubAccountAndTrustData
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.preferences.HomePreferences
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.MuteProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


class HomeViewModel(
    muteProvider: MuteProvider,
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
        muteProvider = muteProvider,
        scope = viewModelScope,
        subCreator = lazyNostrSubscriber.subCreator
    )

    init {
        paginator.init(setting = setting.value)
    }

    fun handle(action: HomeViewAction) {
        when (action) {
            HomeViewRefresh -> refresh()
            HomeViewAppend -> paginator.append()
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
