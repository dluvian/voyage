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
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.provider.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class HomeViewModel(private val feedProvider: FeedProvider) : ViewModel() {
    private val pageSize = 40
    val isRefreshing = mutableStateOf(false)
    val isAppending = mutableStateOf(false)
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
            val initValue = posts.value.value.take(pageSize)
            posts.value = feedProvider.getFeedFlow(until = getCurrentSecs(), size = pageSize)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initValue)
            delay(DELAY)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }

    private val offset = 15
    private var oldFirstId = ""
    private fun append() {
        val currentFirstId = posts.value.value.firstOrNull()?.id.orEmpty()
        if (posts.value.value.size < pageSize ||
            oldFirstId == currentFirstId ||
            isAppending.value
        ) return
        isAppending.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val newUntil = posts.value.value.takeLast(offset).first().createdAt
            posts.value = feedProvider.getFeedFlow(until = newUntil, size = pageSize)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    posts.value.value.takeLast(offset)
                )
        }.invokeOnCompletion {
            isAppending.value = false
            oldFirstId = currentFirstId
        }
    }
}
