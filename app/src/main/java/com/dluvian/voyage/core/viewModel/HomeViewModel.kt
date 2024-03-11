package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.getCurrentSecs
import com.dluvian.voyage.core.DELAY
import com.dluvian.voyage.core.HomeViewAction
import com.dluvian.voyage.core.HomeViewAppend
import com.dluvian.voyage.core.HomeViewRefresh
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
    val posts: MutableState<StateFlow<List<RootPost>>> = mutableStateOf(
        feedProvider.getFeedFlow(until = getCurrentSecs(), size = pageSize)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    )

    fun handle(homeViewAction: HomeViewAction) {
        when (homeViewAction) {
            is HomeViewRefresh -> refresh()
            is HomeViewAppend -> append()
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return

        isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val initValue = coldPosts.value.ifEmpty { posts.value.value }.take(pageSize)
            coldPosts.value = emptyList()
            posts.value = feedProvider.getFeedFlow(until = getCurrentSecs(), size = pageSize)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initValue)
            delay(DELAY)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }

    private fun append() {
        if (posts.value.value.size < pageSize) return
        if (isAppending.value) return

        isAppending.value = true
        viewModelScope.launch(Dispatchers.IO) {
            coldPosts.value += posts.value.value
            val until = posts.value.value.last().createdAt
            posts.value = feedProvider.getFeedFlow(until = until, size = pageSize)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
            delay(SHORT_DELAY)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }
}
