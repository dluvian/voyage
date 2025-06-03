package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.cmd.BookmarksViewAction
import com.dluvian.voyage.cmd.BookmarksViewAppend
import com.dluvian.voyage.cmd.BookmarksViewInit
import com.dluvian.voyage.cmd.BookmarksViewRefresh
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.filterSetting.BookmarkFeedSetting
import com.dluvian.voyage.filterSetting.PostDetails
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.provider.IEventUpdate

class BookmarksViewModel(
    feedProvider: FeedProvider,
    val feedState: LazyListState,
    val postDetails: State<PostDetails?>,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) : ViewModel(), IEventUpdate {
    val paginator = Paginator(
        feedProvider = feedProvider,
        subCreator = lazyNostrSubscriber.subCreator,
        scope = viewModelScope,
    )

    fun handle(action: BookmarksViewAction) {
        when (action) {
            is BookmarksViewInit -> paginator.init(setting = BookmarkFeedSetting)
            is BookmarksViewRefresh -> refresh()
            is BookmarksViewAppend -> paginator.nextPage()
        }
    }

    private fun refresh() {
        viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubMyBookmarks()
        }
        paginator.refresh()
    }
}
