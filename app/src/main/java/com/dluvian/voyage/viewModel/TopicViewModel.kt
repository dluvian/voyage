package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.Topic
import com.dluvian.voyage.cmd.TopicViewAction
import com.dluvian.voyage.cmd.TopicViewAppend
import com.dluvian.voyage.cmd.TopicViewLoadLists
import com.dluvian.voyage.cmd.TopicViewRefresh
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.normalizeTopic
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.filterSetting.ItemSetMeta
import com.dluvian.voyage.filterSetting.PostDetails
import com.dluvian.voyage.filterSetting.TopicFeedSetting
import com.dluvian.voyage.paginator.Paginator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TopicViewModel(
    feedProvider: FeedProvider,
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
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    fun openTopic(topic: Topic) {
        val stripped = topicNavView.topic.normalizeTopic()
        subCreator.unsubAll()
        paginator.reinit(setting = TopicFeedSetting(topic = stripped))

        val initFollowVal = if (currentTopic.value == stripped) isFollowed.value else false
        currentTopic.value = stripped

        isFollowed = topicProvider.getIsFollowedFlow(topic = stripped)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initFollowVal)
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
