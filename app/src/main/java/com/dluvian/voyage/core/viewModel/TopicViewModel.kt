package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.TopicViewAction
import com.dluvian.voyage.core.TopicViewAppend
import com.dluvian.voyage.core.TopicViewLoadLists
import com.dluvian.voyage.core.TopicViewRefresh
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.normalizeTopic
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.MuteProvider
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TopicViewModel(
    feedProvider: FeedProvider,
    muteProvider: MuteProvider,
    val postDetails: State<PostDetails?>,
    val feedState: LazyListState,
    private val subCreator: SubscriptionCreator,
    private val topicProvider: TopicProvider,
    private val itemSetProvider: ItemSetProvider,
) : ViewModel() {
    val addableLists = mutableStateOf(emptyList<ItemSetMeta>())
    val nonAddableLists = mutableStateOf(emptyList<ItemSetMeta>())
    val currentTopic = mutableStateOf("")
    var isFollowed: StateFlow<Boolean> = MutableStateFlow(false)
    var isMuted: StateFlow<Boolean> = MutableStateFlow(false)
    val paginator = Paginator(
        feedProvider = feedProvider,
        muteProvider = muteProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    fun openTopic(topicNavView: TopicNavView) {
        val stripped = topicNavView.topic.normalizeTopic()
        subCreator.unsubAll()
        paginator.reinit(setting = TopicFeedSetting(topic = stripped))

        val initFollowVal = if (currentTopic.value == stripped) isFollowed.value else false
        val initMuteVal = if (currentTopic.value == stripped) isMuted.value else false
        currentTopic.value = stripped

        isFollowed = topicProvider.getIsFollowedFlow(topic = stripped)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initFollowVal)
        isMuted = topicProvider.getIsMutedFlow(topic = stripped)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initMuteVal)
    }

    fun handle(action: TopicViewAction) {
        when (action) {
            TopicViewRefresh -> {
                subCreator.unsubAll()
                paginator.refresh()
            }

            TopicViewAppend -> paginator.append()
            TopicViewLoadLists -> updateLists(topic = currentTopic.value)
        }
    }

    private fun updateLists(topic: Topic) {
        viewModelScope.launchIO {
            addableLists.value = itemSetProvider
                .getAddableSets(item = ItemSetTopic(topic = topic))
            nonAddableLists.value = itemSetProvider
                .getNonAddableSets(item = ItemSetTopic(topic = topic))
        }
    }
}
