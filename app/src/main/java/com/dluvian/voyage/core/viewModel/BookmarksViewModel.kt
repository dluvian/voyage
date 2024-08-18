package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.BookmarksViewAction
import com.dluvian.voyage.core.BookmarksViewAppend
import com.dluvian.voyage.core.BookmarksViewInit
import com.dluvian.voyage.core.BookmarksViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.MuteProvider

class BookmarksViewModel(
    feedProvider: FeedProvider,
    muteProvider: MuteProvider,
    val feedState: LazyListState,
    val postDetails: State<PostDetails?>,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) : ViewModel() {
    val paginator = Paginator(
        feedProvider = feedProvider,
        muteProvider = muteProvider,
        subCreator = lazyNostrSubscriber.subCreator,
        scope = viewModelScope,
    )

    fun handle(action: BookmarksViewAction) {
        when (action) {
            is BookmarksViewInit -> paginator.init(setting = BookmarksFeedSetting)
            is BookmarksViewRefresh -> refresh()
            is BookmarksViewAppend -> paginator.append()
        }
    }

    private fun refresh() {
        viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubMyBookmarks()
        }
        paginator.refresh()
    }
}
