package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.FollowListsViewAction
import com.dluvian.voyage.core.FollowListsViewInit
import com.dluvian.voyage.core.FollowListsViewRefresh
import com.dluvian.voyage.core.model.TopicFollowState
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FollowListsViewModel(
    val contactListState: LazyListState,
    val topicListState: LazyListState,
    val pagerState: PagerState,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val profileProvider: ProfileProvider,
    private val topicProvider: TopicProvider,
) : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
    val isRefreshing = mutableStateOf(false)
    val profiles: MutableState<StateFlow<List<AdvancedProfileView>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val topics: MutableState<StateFlow<List<TopicFollowState>>> =
        mutableStateOf(MutableStateFlow(emptyList()))

    fun handle(action: FollowListsViewAction) {
        when (action) {
            is FollowListsViewInit -> init()
            is FollowListsViewRefresh -> refresh(isInit = false)
        }
    }

    private var isInitialized = false
    private fun init() {
        if (isInitialized) return
        refresh(isInit = true)
        isInitialized = true
    }

    private fun refresh(isInit: Boolean) {
        if (isRefreshing.value) return
        isRefreshing.value = true

        viewModelScope.launch {
            if (!isInit) {
                lazyNostrSubscriber.lazySubMyAccount()
                delay(DEBOUNCE)
            }
            profiles.value = profileProvider.getMyFriendsFlow()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), profiles.value.value)
            topics.value = topicProvider.getMyTopicsFlow()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), topics.value.value)
            delay(DEBOUNCE)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }
}
