package com.dluvian.voyage.core.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.TopicViewAction
import com.dluvian.voyage.core.TopicViewAppend
import com.dluvian.voyage.core.TopicViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.FeedProvider

class TopicViewModel(feedProvider: FeedProvider) : ViewModel() {
    val paginator = Paginator(feedProvider = feedProvider, scope = viewModelScope)

    fun openTopic(topicNavView: TopicNavView) {
        val stripped = topicNavView.topic.removePrefix("#")
        paginator.init(setting = TopicFeedSetting(topic = stripped))
    }

    fun handle(topicViewAction: TopicViewAction) {
        when (topicViewAction) {
            is TopicViewRefresh -> paginator.refresh()
            is TopicViewAppend -> paginator.append()
        }
    }
}
