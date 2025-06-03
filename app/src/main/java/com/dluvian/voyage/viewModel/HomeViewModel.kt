package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.model.HomeViewAccountData
import com.dluvian.voyage.model.HomeViewApplyFilter
import com.dluvian.voyage.model.HomeViewCmd
import com.dluvian.voyage.model.HomeViewDismissFilter
import com.dluvian.voyage.model.HomeViewEventUpdate
import com.dluvian.voyage.model.HomeViewNextPage
import com.dluvian.voyage.model.HomeViewOpenFilter
import com.dluvian.voyage.model.HomeViewPop
import com.dluvian.voyage.model.HomeViewPush
import com.dluvian.voyage.model.HomeViewRefresh
import com.dluvian.voyage.nostr.Subscriber
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.preferences.HomePreferences
import com.dluvian.voyage.provider.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Timestamp
import java.util.concurrent.atomic.AtomicBoolean


class HomeViewModel(
    val feedState: LazyListState,
    private val feedProvider: FeedProvider,
    private val subscriber: Subscriber,
    private val homePreferences: HomePreferences,
) : ViewModel() {
    val showFilterMenu: MutableState<Boolean> = mutableStateOf(false)
    val setting: MutableState<HomeFeedSetting> =
        mutableStateOf(homePreferences.getHomeFeedSetting())
    val paginator = Paginator(feedProvider = feedProvider)

    private var lastDataSub = Timestamp.fromSecs(0u)
    private val isSubbingData = AtomicBoolean(false)

    init {
        paginator.initSetting(setting.value)
    }

    fun handle(cmd: HomeViewCmd) {
        when (cmd) {
            HomeViewPush, HomeViewPop -> viewModelScope.launch(Dispatchers.IO) {
                paginator.dbRefreshInPlace()
            }

            is HomeViewEventUpdate -> viewModelScope.launch(Dispatchers.IO) {
                paginator.update(cmd.event)
            }

            HomeViewRefresh -> viewModelScope.launch(Dispatchers.IO) {
                subscriber.unsubAll()
                paginator.refresh()
            }

            HomeViewNextPage -> viewModelScope.launch(Dispatchers.IO) {
                paginator.nextPage()
            }

            HomeViewAccountData -> {
                if (isSubbingData.compareAndSet(false, true)) {
                    if (Timestamp.now().asSecs() - lastDataSub.asSecs() > 10u) {
                        viewModelScope.launch(Dispatchers.IO) {
                            subscriber.subMyData()
                        }.invokeOnCompletion {
                            lastDataSub = Timestamp.now()
                            isSubbingData.set(false)
                        }
                    }
                }
            }

            HomeViewOpenFilter -> showFilterMenu.value = true
            HomeViewDismissFilter -> showFilterMenu.value = false

            is HomeViewApplyFilter -> if (setting.value != cmd.setting) {
                homePreferences.setHomeFeedSettings(setting = cmd.setting)
                showFilterMenu.value = false
                setting.value = cmd.setting
                paginator.initSetting(setting = cmd.setting)
                viewModelScope.launch(Dispatchers.IO) {
                    paginator.refresh()
                }
            }
        }
    }
}
