package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.MuteListViewAction
import com.dluvian.voyage.core.MuteListViewInit
import com.dluvian.voyage.core.MuteListViewRefresh
import com.dluvian.voyage.core.model.TopicFollowState
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MuteListViewModel @OptIn(ExperimentalFoundationApi::class) constructor(
    val mutedProfileState: LazyListState,
    val mutedTopicState: LazyListState,
    val pagerState: PagerState,
) : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
    val isRefreshing = mutableStateOf(false)
    val mutedProfiles: MutableState<StateFlow<List<AdvancedProfileView>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val mutedTopics: MutableState<StateFlow<List<TopicFollowState>>> =
        mutableStateOf(MutableStateFlow(emptyList()))

    fun handle(action: MuteListViewAction) {
        when (action) {
            is MuteListViewInit -> init()
            is MuteListViewRefresh -> refresh(isInit = false)
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
                TODO("Sub mutes")
                delay(DEBOUNCE)
            }
            mutedProfiles.value = TODO("Get muted profiles")
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    mutedProfiles.value.value
                )
            mutedTopics.value = TODO("Get muted topics")
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mutedTopics.value.value)
            delay(DEBOUNCE)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }
}
