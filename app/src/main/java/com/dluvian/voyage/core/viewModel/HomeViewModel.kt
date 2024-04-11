package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.HomeViewAction
import com.dluvian.voyage.core.HomeViewAppend
import com.dluvian.voyage.core.HomeViewRefresh
import com.dluvian.voyage.core.HomeViewSubAccountAndTrustData
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import kotlinx.coroutines.Job


class HomeViewModel(
    feedProvider: FeedProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    val feedState: LazyListState,
) : ViewModel() {
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = lazyNostrSubscriber.subCreator
    )

    init {
        paginator.init(setting = HomeFeedSetting)
    }

    fun handle(action: HomeViewAction) {
        when (action) {
            is HomeViewRefresh -> refresh()
            is HomeViewAppend -> paginator.append()
            is HomeViewSubAccountAndTrustData -> subMyAccountAndTrustData()
        }
    }

    private var job: Job? = null

    private fun subMyAccountAndTrustData() {
        job?.cancel()
        job = viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubMyAccountAndTrustData()
        }
    }

    private fun refresh() {
        lazyNostrSubscriber.subCreator.unsubAll()
        paginator.refresh(onSub = { subMyAccountAndTrustData() })
    }
}
