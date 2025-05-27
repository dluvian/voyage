package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.BookmarksViewAction
import com.dluvian.voyage.BookmarksViewAppend
import com.dluvian.voyage.BookmarksViewInit
import com.dluvian.voyage.BookmarksViewRefresh
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.filterSetting.BookmarksFeedSetting
import com.dluvian.voyage.filterSetting.PostDetails
import com.dluvian.voyage.paginator.Paginator

class BookmarksViewModel(
    feedProvider: FeedProvider,
    val feedState: LazyListState,
    val postDetails: State<PostDetails?>,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) : ViewModel() {
    val paginator = Paginator(
        feedProvider = feedProvider,
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
