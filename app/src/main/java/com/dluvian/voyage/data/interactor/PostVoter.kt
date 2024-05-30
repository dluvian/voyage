package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickDownvote
import com.dluvian.voyage.core.ClickNeutralizeVote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.VoteEvent
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.EventDeletor
import com.dluvian.voyage.data.nostr.NostrService
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
import rust.nostr.protocol.Kind
import rust.nostr.protocol.PublicKey

private const val TAG = "PostVoter"

class PostVoter(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
    private val context: Context,
    private val voteDao: VoteDao,
    private val eventDeletor: EventDeletor,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _forcedVotes = MutableStateFlow(mapOf<EventIdHex, Vote>())

    val forcedVotes = _forcedVotes
        .stateIn(scope, SharingStarted.Eagerly, _forcedVotes.value)

    fun handle(action: VoteEvent) {
        val newVote = when (action) {
            is ClickUpvote -> Upvote
            is ClickDownvote -> Downvote
            is ClickNeutralizeVote -> NoVote
        }
        updateForcedVote(action.postId, newVote)
        vote(
            postId = action.postId,
            mention = action.mention,
            vote = newVote,
            kind = 1u, // We currently only vote on kind 1. Reposts are dereferenced to their kind 1
        )
    }

    private fun updateForcedVote(postId: EventIdHex, newVote: Vote) {
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
        vote: Vote,
        kind: UShort,
    ) {
        jobs[postId]?.cancel(CancellationException("User clicks fast"))
        jobs[postId] = scope.launch {
            delay(DEBOUNCE)
            val currentVote = voteDao.getMyVote(postId = postId)
            when (vote) {
                Upvote, Downvote -> handleVote(
                    currentVote = currentVote,
                    postId = postId,
                    mention = mention,
                    isPositive = vote.isPositive(),
                    kind = kind,
                )

                NoVote -> {
                    if (currentVote == null) return@launch
                    eventDeletor.deleteVote(voteId = currentVote.id)
                }
            }
        }
        jobs[postId]?.invokeOnCompletion { ex ->
            if (ex == null) Log.d(TAG, "Successfully voted $vote on $postId")
            else Log.d(TAG, "Failed to vote $vote on $postId: ${ex.message}")
        }
    }

    private suspend fun handleVote(
        currentVote: VoteEntity?,
        postId: EventIdHex,
        mention: PubkeyHex,
        isPositive: Boolean,
        kind: UShort,
    ) {
        if (currentVote?.isPositive == isPositive) return
        if (currentVote != null) {
            eventDeletor.deleteVote(voteId = currentVote.id)
        }
        nostrService.publishVote(
            eventId = EventId.fromHex(postId),
            mention = PublicKey.fromHex(mention),
            isPositive = isPositive,
            kind = Kind(kind),
            relayUrls = relayProvider.getPublishRelays(publishTo = listOf(mention)),
        )
            .onSuccess { event ->
                val entity = VoteEntity(
                    id = event.id().toHex(),
                    postId = postId,
                    pubkey = event.author().toHex(),
                    isPositive = isPositive,
                    createdAt = event.createdAt().secs(),
                )
                voteDao.insertOrReplaceVote(voteEntity = entity)
            }
            .onFailure {
                Log.w(TAG, "Failed to publish vote: ${it.message}", it)
                updateForcedVote(postId = postId, newVote = NoVote)
                snackbar.showToast(
                    scope = scope,
                    msg = context.getString(R.string.failed_to_sign_vote)
                )
            }
    }
}
