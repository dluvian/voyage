package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.BookmarkEvent
import com.dluvian.voyage.core.BookmarkPost
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.LIST_CHANGE_DEBOUNCE
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.UnbookmarkPost
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.ValidatedBookmarkList
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.BookmarkDao
import com.dluvian.voyage.data.room.dao.tx.BookmarkUpsertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val TAG = "Bookmarker"

class Bookmarker(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val bookmarkUpsertDao: BookmarkUpsertDao,
    private val bookmarkDao: BookmarkDao,
    private val snackbar: SnackbarHostState,
    private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _forcedBookmarks = MutableStateFlow(mapOf<EventIdHex, Boolean>())
    val forcedBookmarksFlow = _forcedBookmarks.stateIn(
        scope,
        SharingStarted.Eagerly,
        _forcedBookmarks.value
    )

    fun handle(action: BookmarkEvent) {
        when (action) {
            is BookmarkPost -> handleAction(
                postId = action.postId,
                isBookmarked = true,
            )

            is UnbookmarkPost -> handleAction(
                postId = action.postId,
                isBookmarked = false,
            )
        }
    }

    private fun handleAction(postId: EventIdHex, isBookmarked: Boolean) {
        updateForcedStates(postId = postId, isFollowed = isBookmarked)
        handleBookmark()
    }

    private fun updateForcedStates(postId: EventIdHex, isFollowed: Boolean) {
        _forcedBookmarks.update {
            val mutable = it.toMutableMap()
            mutable[postId] = isFollowed
            mutable
        }
    }

    private var job: Job? = null
    private fun handleBookmark() {
        if (job?.isActive == true) return

        job = scope.launchIO {
            delay(LIST_CHANGE_DEBOUNCE)

            val toHandle = _forcedBookmarks.value.toMap()
            val before = bookmarkDao.getMyBookmarks().toSet()
            val adjusted = before.toMutableSet()
            val toAdd = toHandle.filter { (_, bool) -> bool }.map { (id, _) -> id }
            adjusted.addAll(toAdd)
            val toRemove = toHandle.filterNot { (_, bool) -> bool }.map { (id, _) -> id }
            adjusted.removeAll(toRemove.toSet())

            if (before == adjusted) return@launchIO

            if (adjusted.size > MAX_KEYS_SQL && adjusted.size > before.size) {
                Log.w(TAG, "New bookmark list is too large (${adjusted.size})")
                adjusted
                    .minus(before)
                    .forEach { updateForcedStates(postId = it, isFollowed = false) }
                val msg = context.getString(
                    R.string.bookmarking_more_than_n_is_not_allowed,
                    MAX_KEYS_SQL
                )
                snackbar.showToast(scope = scope, msg = msg)
                return@launchIO
            }

            nostrService.publishBookmarkList(
                postIds = adjusted.toList(),
                relayUrls = relayProvider.getPublishRelays(addConnected = false),
            ).onSuccess { event ->
                val bookmarks = ValidatedBookmarkList(
                    myPubkey = event.author().toHex(),
                    postIds = event.eventIds().map { it.toHex() }.toSet(),
                    createdAt = event.createdAt().secs()
                )
                bookmarkUpsertDao.upsertBookmarks(validatedBookmarkList = bookmarks)
            }
                .onFailure {
                    Log.w(TAG, "Failed to publish bookmarks: ${it.message}", it)
                    snackbar.showToast(
                        scope = scope,
                        msg = context.getString(R.string.failed_to_sign_topic_list)
                    )
                }
        }
    }
}
