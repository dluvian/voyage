package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(feedProvider: FeedProvider) : ViewModel() {
    val isRefreshing = mutableStateOf(false)
    val page = mutableStateOf(0)
    var posts: StateFlow<List<RootPost>> = feedProvider.getFeedFlow(page.value)
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
