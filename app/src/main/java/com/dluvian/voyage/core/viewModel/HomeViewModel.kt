package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.getCurrentSecs
import com.dluvian.voyage.core.DELAY
import com.dluvian.voyage.core.SHORT_DELAY
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.provider.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class HomeViewModel(private val feedProvider: FeedProvider) : ViewModel() {
    val pageSize = 25
    val isRefreshing = mutableStateOf(false)
    val isAppending = mutableStateOf(false)
    val coldPosts = mutableStateOf(emptyList<RootPost>())
    var posts: StateFlow<List<RootPost>> =
        feedProvider.getFeedFlow(until = getCurrentSecs(), size = pageSize, isRefresh = false)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun refresh() {
        if (isRefreshing.value) return

        isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            posts = feedProvider.getFeedFlow(
                until = getCurrentSecs(),
                size = pageSize,
                isRefresh = true
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), posts.value)
            delay(DELAY)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }

    fun append() {
        if (posts.value.size < pageSize) return
        if (isAppending.value) return

        isAppending.value = true
        viewModelScope.launch(Dispatchers.IO) {
            coldPosts.value += posts.value
            posts = feedProvider.getFeedFlow(
                until = getCurrentSecs(),
                size = pageSize,
                isRefresh = false
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), posts.value)
            delay(SHORT_DELAY)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }
}
