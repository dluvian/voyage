package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.InboxViewAction
import com.dluvian.voyage.core.InboxViewAppend
import com.dluvian.voyage.core.InboxViewInit
import com.dluvian.voyage.core.InboxViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import java.util.concurrent.atomic.AtomicBoolean

class InboxViewModel(
    feedProvider: FeedProvider,
    subCreator: SubscriptionCreator,
    val feedState: LazyListState,
) : ViewModel() {
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    fun handle(action: InboxViewAction) {
        when (action) {
            is InboxViewInit -> init()
            is InboxViewRefresh -> paginator.refresh()
            is InboxViewAppend -> paginator.append()
        }
    }

    private val isInitialized = AtomicBoolean(false)
    private fun init() {
        if (!isInitialized.compareAndSet(false, true)) return
        paginator.init(InboxFeedSetting)
    }
}