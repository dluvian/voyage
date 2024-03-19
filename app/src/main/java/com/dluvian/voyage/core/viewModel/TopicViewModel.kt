package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.TopicViewAction
import com.dluvian.voyage.core.TopicViewAppend
import com.dluvian.voyage.core.TopicViewFollowTopic
import com.dluvian.voyage.core.TopicViewRefresh
import com.dluvian.voyage.core.TopicViewUnfollowTopic
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.data.interactor.TopicFollower
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.TopicProvider

class TopicViewModel(
    feedProvider: FeedProvider,
    private val topicProvider: TopicProvider,
    private val topicFollower: TopicFollower,
) : ViewModel() {
    val currentTopic = mutableStateOf("")
    val isFollowed = mutableStateOf(false)
    val paginator = Paginator(feedProvider = feedProvider, scope = viewModelScope)

    fun openTopic(topicNavView: TopicNavView) {
        val stripped = topicNavView.topic.removePrefix("#")
        paginator.init(setting = TopicFeedSetting(topic = stripped))
        currentTopic.value = stripped
        isFollowed.value = topicFollower.forcedStates.value[stripped]
            ?: topicProvider.isFollowed(topic = stripped)
    }

    fun handle(action: TopicViewAction) {
        when (action) {
            is TopicViewRefresh -> paginator.refresh()
            is TopicViewAppend -> paginator.append()
            is TopicViewFollowTopic -> {
                topicFollower.follow(topic = action.topic)
                isFollowed.value = true
            }

            is TopicViewUnfollowTopic -> {
                topicFollower.unfollow(topic = action.topic)
                isFollowed.value = false
            }
        }
    }
}
