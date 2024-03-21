package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DiscoverViewAction
import com.dluvian.voyage.core.DiscoverViewRefresh
import com.dluvian.voyage.core.model.TopicFollowState
import com.dluvian.voyage.data.model.FullProfile
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val topicProvider: TopicProvider,
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    private val maxDisplayCount = 20
    val isRefreshing = mutableStateOf(false)
    val popularTopics: MutableState<List<TopicFollowState>> = mutableStateOf(emptyList())
    val popularProfiles: MutableState<List<FullProfile>> = mutableStateOf(emptyList())

    fun handle(action: DiscoverViewAction) {
        when (action) {
            is DiscoverViewRefresh -> refresh()
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true

        viewModelScope.launch {
            val topicJob = viewModelScope.launch(Dispatchers.IO) {
                popularTopics.value = topicProvider
                    .getPopularUnfollowedTopics(limit = maxDisplayCount)
                    .map { TopicFollowState(topic = it, isFollowed = false) }
            }
            val profileJob = viewModelScope.launch(Dispatchers.IO) {
                popularProfiles.value = profileProvider
                    .getPopularUnfollowedProfiles(limit = maxDisplayCount)
            }
            joinAll(topicJob, profileJob)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }
}
