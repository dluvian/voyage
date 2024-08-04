package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickNeutralizeVote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.VoteEvent
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.event.EventDeletor
import com.dluvian.voyage.data.event.EventRebroadcaster
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.secs
import com.dluvian.voyage.data.preferences.EventPreferences
import com.dluvian.voyage.data.preferences.RelayPreferences
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.VoteDao
import com.dluvian.voyage.data.room.entity.VoteEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.PublicKey

private const val TAG = "PostVoter"

class PostVoter(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
    private val context: Context,
    private val voteDao: VoteDao,
    private val eventDeletor: EventDeletor,
    private val rebroadcaster: EventRebroadcaster,
    private val relayPreferences: RelayPreferences,
    private val eventPreferences: EventPreferences,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _forcedVotes = MutableStateFlow(mapOf<EventIdHex, Boolean>())

    val forcedVotes = _forcedVotes
        .stateIn(scope, SharingStarted.Eagerly, _forcedVotes.value)

    fun handle(action: VoteEvent) {
        val newVote = when (action) {
            is ClickUpvote -> true
            is ClickNeutralizeVote -> false
        }
        updateForcedVote(action.postId, newVote)
        if (relayPreferences.getSendUpvotedToLocalRelay()) {
            scope.launchIO { rebroadcaster.rebroadcastLocally(postId = action.postId) }
        }
        vote(
            postId = action.postId,
            mention = action.mention,
            isUpvote = newVote,
        )
    }

    private fun updateForcedVote(postId: EventIdHex, newVote: Boolean) {
        _forcedVotes.update {
            val mutable = it.toMutableMap()
            mutable[postId] = newVote
            mutable
        }
    }

    private val jobs: MutableMap<EventIdHex, Job?> = mutableMapOf()
    private fun vote(
        postId: EventIdHex,
        mention: PubkeyHex,
        isUpvote: Boolean,
    ) {
        jobs[postId]?.cancel(CancellationException("User clicks fast"))
        jobs[postId] = scope.launch {
            delay(DEBOUNCE)
            val currentVote = voteDao.getMyVote(postId = postId)
            if (isUpvote) {
                upvote(
                    currentVote = currentVote,
                    postId = postId,
                    mention = mention,
                )
            } else {
                if (currentVote == null) return@launch
                eventDeletor.deleteVote(voteId = currentVote.id)
            }
        }
        jobs[postId]?.invokeOnCompletion { ex ->
            if (ex == null) Log.d(TAG, "Successfully voted $isUpvote on $postId")
            else Log.d(TAG, "Failed to vote $isUpvote on $postId: ${ex.message}")
        }
    }

    private suspend fun upvote(
        currentVote: VoteEntity?,
        postId: EventIdHex,
        mention: PubkeyHex,
    ) {
        if (currentVote != null) {
            eventDeletor.deleteVote(voteId = currentVote.id)
        }
        nostrService.publishVote(
            eventId = EventId.fromHex(postId),
            content = eventPreferences.getUpvoteContent(),
            mention = PublicKey.fromHex(mention),
            relayUrls = relayProvider.getPublishRelays(
                publishTo = listOf(mention),
                addConnected = false
            ),
        )
            .onSuccess { event ->
                val entity = VoteEntity(
                    id = event.id().toHex(),
                    postId = postId,
                    pubkey = event.author().toHex(),
                    createdAt = event.createdAt().secs(),
                )
                voteDao.insertOrReplaceVote(voteEntity = entity)
            }
            .onFailure {
                Log.w(TAG, "Failed to publish vote: ${it.message}", it)
                updateForcedVote(postId = postId, newVote = false)
                snackbar.showToast(
                    scope = scope,
                    msg = context.getString(R.string.failed_to_sign_vote)
                )
            }
    }
}
