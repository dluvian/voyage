package com.dluvian.voyage.viewModel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.BookmarkFeedSetting
import com.dluvian.voyage.model.BookmarkViewCmd
import com.dluvian.voyage.model.BookmarkViewEventUpdate
import com.dluvian.voyage.model.BookmarkViewOpen
import com.dluvian.voyage.model.BookmarksViewNextPage
import com.dluvian.voyage.model.BookmarksViewRefresh
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.provider.FeedProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

class BookmarkViewModel(
    feedProvider: FeedProvider,
    val feedState: LazyListState,
    private val service: NostrService,
) : ViewModel() {
    private val logTag = "BookmarkViewModel"
    val paginator = Paginator(feedProvider)
    private val isInitialized = AtomicBoolean(false)

    init {
        paginator.initSetting(setting = BookmarkFeedSetting(PAGE_SIZE.toULong()))
    }

    fun handle(cmd: BookmarkViewCmd) {
        when (cmd) {
            BookmarkViewOpen -> {
                viewModelScope.launch(Dispatchers.IO) {
                    subBookmarks()
                }
                if (isInitialized.compareAndSet(false, true)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        paginator.refresh()
                    }
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    paginator.dbRefreshInPlace()
                }
            }

            is BookmarkViewEventUpdate -> viewModelScope.launch {
                paginator.update(cmd.event)
            }

            is BookmarksViewRefresh -> {
                viewModelScope.launch {
                    subBookmarks()
                }
                viewModelScope.launch {
                    paginator.refresh()
                }
            }

            is BookmarksViewNextPage -> viewModelScope.launch {
                paginator.nextPage()
            }
        }
    }

    private suspend fun subBookmarks() {
        val pubkey = service.pubkey()
        val filter = Filter().author(pubkey).kind(Kind.fromStd(KindStandard.BOOKMARKS)).limit(1u)
        val event = service.dbQuery(filter).firstOrNull()
        if (event == null) {
            Log.i(logTag, "No bookmark event found")
            service.subscribe(filter)
            return
        }
        // TODO: Upstream
        val since = event.createdAt().asSecs() + 1u
        val newFilter = filter.since(Timestamp.fromSecs(since))
        service.subscribe(newFilter)
    }
}
