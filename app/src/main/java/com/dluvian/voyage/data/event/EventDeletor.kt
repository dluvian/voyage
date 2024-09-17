package com.dluvian.voyage.data.event

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.R
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.util.DeleteDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId

private const val TAG = "EventDeletor"

class EventDeletor(
    private val snackbar: SnackbarHostState,
    private val nostrService: NostrService,
    private val context: Context,
    private val relayProvider: RelayProvider,
    private val deleteDao: DeleteDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun deletePost(postId: EventIdHex) {
        deleteEvent(eventId = postId)
            .onFailure {
                Log.w(TAG, "Failed to sign post deletion: ${it.message}", it)
                snackbar.showToast(
                    scope = scope,
                    msg = context.getString(R.string.failed_to_sign_post_deletion)
                )
            }
            .onSuccess {
                deleteDao.deletePost(postId = postId)
            }
    }

    suspend fun deleteVote(voteId: EventIdHex) {
        deleteEvent(eventId = voteId)
            .onFailure {
                Log.w(TAG, "Failed to sign vote deletion: ${it.message}", it)
                snackbar.showToast(
                    scope = scope,
                    msg = context.getString(R.string.failed_to_sign_vote_deletion)
                )
            }
            .onSuccess {
                deleteDao.deleteVote(voteId = voteId)
            }
    }

    suspend fun deleteList(identifier: String, onCloseDrawer: Fn) {
        deleteListEvent(identifier = identifier)
            .onFailure {
                Log.w(TAG, "Failed to sign list deletion: ${it.message}", it)
                snackbar.showToast(
                    scope = scope,
                    msg = context.getString(R.string.failed_to_sign_list_deletion)
                )
                onCloseDrawer()
            }
            .onSuccess {
                deleteDao.deleteList(identifier = identifier)
            }
    }

    private suspend fun deleteEvent(eventId: EventIdHex): Result<Event> {
        return nostrService.publishDelete(
            eventId = EventId.fromHex(eventId),
            relayUrls = relayProvider.getPublishRelays(),
        )
    }

    private suspend fun deleteListEvent(identifier: String): Result<Event> {
        return nostrService.publishListDeletion(
            identifier = identifier,
            relayUrls = relayProvider.getPublishRelays(),
        )
    }
}
