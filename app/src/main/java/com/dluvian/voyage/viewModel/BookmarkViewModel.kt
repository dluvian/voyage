package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.BookmarkFeedSetting
import com.dluvian.voyage.model.BookmarkViewCmd
import com.dluvian.voyage.model.BookmarkViewEventUpdate
import com.dluvian.voyage.model.BookmarkViewPop
import com.dluvian.voyage.model.BookmarkViewPush
import com.dluvian.voyage.model.BookmarksViewNextPage
import com.dluvian.voyage.model.BookmarksViewRefresh
import com.dluvian.voyage.nostr.Subscriber
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.provider.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class BookmarkViewModel(
    feedProvider: FeedProvider,
    val feedState: LazyListState,
    private val subscriber: Subscriber,
) : ViewModel() {
    val paginator = Paginator(feedProvider)
    val hasPushedAtLeastOnce = AtomicBoolean(false)

    init {
        paginator.initSetting(setting = BookmarkFeedSetting(PAGE_SIZE.toULong()))
    }

    fun handle(cmd: BookmarkViewCmd) {
        when (cmd) {
            BookmarkViewPush -> {
                if (hasPushedAtLeastOnce.compareAndSet(false, true)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        subscriber.subBookmarks()
                        paginator.refresh()
                    }
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    paginator.dbRefreshInPlace()
                }
            }

            BookmarkViewPop -> viewModelScope.launch {
                paginator.dbRefreshInPlace()
            }

            is BookmarkViewEventUpdate -> viewModelScope.launch {
                paginator.update(cmd.event)
            }

            is BookmarksViewRefresh -> viewModelScope.launch {
                subscriber.subBookmarks()
                paginator.refresh()
            }

            is BookmarksViewNextPage -> viewModelScope.launch {
                paginator.nextPage()
            }
        }
    }
}
