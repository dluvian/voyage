package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.provider.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rust.nostr.protocol.Timestamp

class HomeViewModel(feedProvider: FeedProvider) : ViewModel() {
    val isRefreshing = mutableStateOf(false)
    var posts: StateFlow<List<RootPost>> =
        feedProvider.getFeedFlow(until = Timestamp.now().asSecs().toLong(), size = 25)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun refresh() {
        isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            delay(3000)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }
}
