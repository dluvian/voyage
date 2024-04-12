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
import com.dluvian.voyage.core.SignerLauncher
import com.dluvian.voyage.core.VoteEvent
import com.dluvian.voyage.core.showToast
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
import rust.nostr.protocol.PublicKey

class PostVoter(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
    private val context: Context,
    private val voteDao: VoteDao,
) {
    private val tag = "PostVoter"
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
            kind = 1,
            signerLauncher = action.signerLauncher,
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
        kind: Int,
        signerLauncher: SignerLauncher
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
                    signerLauncher = signerLauncher,
                )

                NoVote -> {
                    if (currentVote == null) return@launch
                    deleteVote(voteId = currentVote.id, signerLauncher = signerLauncher)
                    voteDao.deleteMyVote(postId = postId)
                }
            }
        }
        jobs[postId]?.invokeOnCompletion { ex ->
            if (ex == null) Log.d(tag, "Successfully voted $vote on $postId")
            else Log.d(tag, "Failed to vote $vote on $postId: ${ex.message}")
        }
    }

    private suspend fun handleVote(
        currentVote: VoteEntity?,
        postId: EventIdHex,
        mention: PubkeyHex,
        isPositive: Boolean,
        kind: Int,
        signerLauncher: SignerLauncher,
    ) {
        if (currentVote?.isPositive == isPositive) return
        if (currentVote != null) {
            deleteVote(voteId = currentVote.id, signerLauncher = signerLauncher)
        }
        nostrService.publishVote(
            eventId = EventId.fromHex(postId),
            mention = PublicKey.fromHex(mention),
            isPositive = isPositive,
            kind = kind,
            relayUrls = relayProvider.getPublishRelays(publishTo = listOf(mention)),
            signerLauncher = signerLauncher,
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
                Log.w(tag, "Failed to publish vote: ${it.message}", it)
                updateForcedVote(postId = postId, newVote = NoVote)
                snackbar.showToast(
                    scope = scope,
                    msg = context.getString(R.string.failed_to_sign_vote)
                )
            }
    }

    private suspend fun deleteVote(voteId: EventIdHex, signerLauncher: SignerLauncher) {
        nostrService.publishDelete(
            eventId = EventId.fromHex(voteId),
            relayUrls = relayProvider.getPublishRelays(),
            signerLauncher = signerLauncher,
        ).onFailure {
            Log.w(tag, "Failed to delete vote: ${it.message}", it)
            snackbar.showToast(
                scope = scope,
                msg = context.getString(R.string.failed_to_sign_vote_deletion)
            )
        }
    }
}
