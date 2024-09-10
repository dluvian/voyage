package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.ListViewAction
import com.dluvian.voyage.core.ListViewFeedAppend
import com.dluvian.voyage.core.ListViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.MuteProvider

class ListViewModel @OptIn(ExperimentalFoundationApi::class) constructor(
    feedProvider: FeedProvider,
    muteProvider: MuteProvider,
    val postDetails: State<PostDetails?>,
    val feedState: LazyListState,
    val profileState: LazyListState,
    val topicState: LazyListState,
    val itemSetProvider: ItemSetProvider,
    val pagerState: PagerState,
    val showAuthorName: State<Boolean>,
    private val lazyNostrSubscriber: LazyNostrSubscriber
) : ViewModel() {
    val isLoading = mutableStateOf(false)
    val tabIndex = mutableIntStateOf(0)

    val paginator = Paginator(
        feedProvider = feedProvider,
        muteProvider = muteProvider,
        scope = viewModelScope,
        subCreator = lazyNostrSubscriber.subCreator
    )

    fun handle(action: ListViewAction) {
        when (action) {
            ListViewRefresh -> paginator.refresh()
            ListViewFeedAppend -> paginator.append()
        }
    }

    fun openList(identifier: String) {
        isLoading.value = true
        paginator.reinit(setting = ListFeedSetting(identifier = identifier))
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
