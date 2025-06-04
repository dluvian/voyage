package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.TopicViewCmd
import com.dluvian.voyage.model.TopicViewNextPage
import com.dluvian.voyage.model.TopicViewPop
import com.dluvian.voyage.model.TopicViewPush
import com.dluvian.voyage.model.TopicViewRefresh
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.provider.FeedProvider
import com.dluvian.voyage.provider.IEventUpdate
import com.dluvian.voyage.provider.TopicProvider
import rust.nostr.sdk.Event

class TopicViewModel(
    feedProvider: FeedProvider,
    val feedState: LazyListState,
    val topicProvider: TopicProvider,
) : ViewModel(), IEventUpdate {
    val currentTopic = mutableStateOf("")
    var isFollowed = mutableStateOf(false)
    val paginator = Paginator(feedProvider)

    fun handle(action: TopicViewCmd) {
        when (action) {
            is TopicViewPop -> TODO()
            is TopicViewPush -> TODO()
            TopicViewNextPage -> TODO()
            TopicViewRefresh -> TODO()
        }
    }

    override suspend fun update(event: Event) {
        TODO("Not yet implemented")
    }
}
