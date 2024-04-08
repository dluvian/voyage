package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.TopicViewAction
import com.dluvian.voyage.core.TopicViewAppend
import com.dluvian.voyage.core.TopicViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TopicViewModel(
    feedProvider: FeedProvider,
    subCreator: SubscriptionCreator,
    private val topicProvider: TopicProvider,
    val feedState: LazyListState,
) : ViewModel() {

    val currentTopic = mutableStateOf("")
    var isFollowed: StateFlow<Boolean> = MutableStateFlow(false)
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )


    fun openTopic(topicNavView: TopicNavView) {
        val stripped = topicNavView.topic.removePrefix("#")
        paginator.init(setting = TopicFeedSetting(topic = stripped))
        val initVal = if (currentTopic.value == stripped) isFollowed.value else false
        currentTopic.value = stripped
        isFollowed = topicProvider.getIsFollowedFlow(topic = topicNavView.topic)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initVal)
    }

    fun handle(action: TopicViewAction) {
        when (action) {
            is TopicViewRefresh -> paginator.refresh()
            is TopicViewAppend -> paginator.append()
        }
    }
}
