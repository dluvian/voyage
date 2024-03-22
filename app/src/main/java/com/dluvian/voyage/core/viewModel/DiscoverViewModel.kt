package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.DiscoverViewAction
import com.dluvian.voyage.core.DiscoverViewFollowProfile
import com.dluvian.voyage.core.DiscoverViewFollowTopic
import com.dluvian.voyage.core.DiscoverViewInit
import com.dluvian.voyage.core.DiscoverViewRefresh
import com.dluvian.voyage.core.DiscoverViewUnfollowProfile
import com.dluvian.voyage.core.DiscoverViewUnfollowTopic
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.TopicFollowState
import com.dluvian.voyage.data.interactor.ProfileFollower
import com.dluvian.voyage.data.interactor.TopicFollower
import com.dluvian.voyage.data.model.FullProfile
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val topicProvider: TopicProvider,
    private val profileProvider: ProfileProvider,
    private val topicFollower: TopicFollower,
    private val profileFollower: ProfileFollower,
) : ViewModel() {
    private val maxDisplayCount = 150
    val isRefreshing = mutableStateOf(false)
    val popularTopics: MutableState<StateFlow<List<TopicFollowState>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val popularProfiles: MutableState<List<FullProfile>> = mutableStateOf(emptyList())

    fun handle(action: DiscoverViewAction) {
        when (action) {
            is DiscoverViewInit -> init()
            is DiscoverViewRefresh -> refresh()
            is DiscoverViewFollowTopic -> topicFollower.follow(action.topic)
            is DiscoverViewUnfollowTopic -> topicFollower.unfollow(action.topic)

            is DiscoverViewFollowProfile -> updateProfileFollowState(
                pubkey = action.pubkey,
                isFollowed = true
            )

            is DiscoverViewUnfollowProfile -> updateProfileFollowState(
                pubkey = action.pubkey,
                isFollowed = false
            )
        }
    }

    private var initJob: Job? = null
    private fun init() {
        if (initJob?.isActive == true) return
        initJob = viewModelScope.launch(Dispatchers.IO) {
            refresh()
            delay(DELAY_10SEC)
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true

        viewModelScope.launch {
            val topicJob = viewModelScope.launch(Dispatchers.IO) {
                popularTopics.value = getTopicFlow()
            }
            val profileJob = viewModelScope.launch(Dispatchers.IO) {
                popularProfiles.value = profileProvider
                    .getPopularUnfollowedProfiles(limit = maxDisplayCount)
            }
            delay(DELAY_1SEC)
            joinAll(topicJob, profileJob)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }

    private fun updateProfileFollowState(pubkey: PubkeyHex, isFollowed: Boolean) {
        if (isFollowed) profileFollower.follow(pubkey) else profileFollower.unfollow(pubkey)
        popularProfiles.value = popularProfiles.value.map {
            val advanced = it.advancedProfile.copy(
                isFriend = profileFollower.forcedFollows.value[pubkey] ?: false
            )
            it.copy(advancedProfile = advanced)
        }
    }

    private suspend fun getTopicFlow(): StateFlow<List<TopicFollowState>> {
        val result = topicProvider.getPopularUnfollowedTopics(limit = maxDisplayCount)
        return topicFollower.forcedStatesFlow.map { forcedStates ->
            result.map { TopicFollowState(topic = it, isFollowed = forcedStates[it] ?: false) }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                popularTopics.value.value
            )
    }
}
