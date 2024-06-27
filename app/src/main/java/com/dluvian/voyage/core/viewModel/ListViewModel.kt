package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.ListViewAction
import com.dluvian.voyage.core.ListViewFeedAppend
import com.dluvian.voyage.core.ListViewRefresh
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider

class ListViewModel(
    feedProvider: FeedProvider,
    subCreator: SubscriptionCreator,
    val itemSetProvider: ItemSetProvider,

) : ViewModel() {
    val isLoading = mutableStateOf(false)
    val tabIndex = mutableIntStateOf(0)

    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    fun handle(action: ListViewAction) {
        when (action) {
            ListViewRefresh -> TODO()
            ListViewFeedAppend -> paginator.append()
        }
    }

    fun openList(identifier: String) {
        isLoading.value = true
        paginator.reinit(setting = ListFeedSetting(identifier = identifier))
        viewModelScope.launchIO {
            itemSetProvider.loadList(identifier = identifier)
        }.invokeOnCompletion {
            isLoading.value = false
        }
    }
}
