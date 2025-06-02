package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.cmd.ListViewAction
import com.dluvian.voyage.cmd.ListViewFeedAppend
import com.dluvian.voyage.cmd.ListViewRefresh
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.filterSetting.CustomPubkeys
import com.dluvian.voyage.filterSetting.ListFeedSetting
import com.dluvian.voyage.filterSetting.ListPubkeys
import com.dluvian.voyage.filterSetting.PostDetails
import com.dluvian.voyage.paginator.Paginator

class ListViewModel(
    feedProvider: FeedProvider,
    val postDetails: State<PostDetails?>,
    val feedState: LazyListState,
    val profileState: LazyListState,
    val topicState: LazyListState,
    val itemSetProvider: ItemSetProvider,
    val pagerState: PagerState,
    private val lazyNostrSubscriber: LazyNostrSubscriber
) : ViewModel() {
    val isLoading = mutableStateOf(false)
    val tabIndex = mutableIntStateOf(0)

    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = lazyNostrSubscriber.subCreator
    )

    fun handle(action: ListViewAction) {
        when (action) {
            ListViewRefresh -> paginator.refresh()
            ListViewFeedAppend -> paginator.nextPage()
        }
    }

    fun openList(identifier: String) {
        isLoading.value = true
        paginator.reinit(setting = ListFeedSetting(ident = identifier))
        viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubNip65s(selection = ListPubkeys(identifier = identifier))
            itemSetProvider.loadList(identifier = identifier)
            lazyNostrSubscriber.lazySubUnknownProfiles(
                selection = CustomPubkeys(itemSetProvider.profiles.value.map { it.pubkey })
            )
        }.invokeOnCompletion {
            isLoading.value = false
        }
    }
}
