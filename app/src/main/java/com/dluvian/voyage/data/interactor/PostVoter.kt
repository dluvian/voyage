package com.dluvian.voyage.data.interactor

import android.util.Log
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.core.ClickDownvote
import com.dluvian.voyage.core.ClickNeutralizeVote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.LONG_DELAY
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.VoteEvent
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.VoteDao
import com.dluvian.voyage.data.room.dao.tx.VoteUpsertDao
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
    private val voteDao: VoteDao,
    private val voteUpsertDao: VoteUpsertDao
) {
    private val tag = "PostVoter"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _forcedVotes = MutableStateFlow(mapOf<EventIdHex, Vote>())

    val forcedVotes = _forcedVotes
        .stateIn(scope, SharingStarted.Eagerly, _forcedVotes.value)

    fun handle(voteEvent: VoteEvent) {
        val newVote = when (voteEvent) {
            is ClickUpvote -> Upvote
            is ClickDownvote -> Downvote
            is ClickNeutralizeVote -> NoVote
        }
        updateForcedVote(voteEvent, newVote)
        vote(
            postId = voteEvent.postId,
            pubkey = voteEvent.pubkey,
            vote = newVote,
            kind = 1
        ) // TODO: Set real kind. Important once reposts are supported
    }

    private fun updateForcedVote(voteEvent: VoteEvent, newVote: Vote) {
        _forcedVotes.update {
            val mutable = it.toMutableMap()
            mutable[voteEvent.postId] = newVote
            mutable
        }
    }

    private val jobs: MutableMap<EventIdHex, Job?> = mutableMapOf()
    fun vote(postId: EventIdHex, pubkey: PubkeyHex, vote: Vote, kind: Int) {
        jobs[postId]?.cancel(CancellationException("User clicks fast"))
        jobs[postId] = scope.launch {
            delay(LONG_DELAY)
            val currentVote = voteDao.getMyVote(postId = postId)
            when (vote) {
                Upvote, Downvote -> handleVote(
                    currentVote = currentVote,
                    postId = postId,
                    pubkey = pubkey,
                    isPositive = vote.isPositive(),
                    kind = kind
                )

                NoVote -> {
                    if (currentVote == null) return@launch
                    nostrService.publishDelete(
                        eventId = EventId.fromHex(currentVote.id),
                        relayUrls = relayProvider.getWriteRelays()
                    )
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
        pubkey: PubkeyHex,
        isPositive: Boolean,
        kind: Int
    ) {
        if (currentVote?.isPositive == isPositive) return
        if (currentVote != null) {
            nostrService.publishDelete(
                eventId = EventId.fromHex(currentVote.id),
                relayUrls = relayProvider.getWriteRelays()
            )
        }
        nostrService.publishVote(
            eventId = EventId.fromHex(postId),
            pubkey = PublicKey.fromHex(pubkey),
            isPositive = isPositive,
            kind = kind,
            relayUrls = relayProvider.getPublishRelays(publishTo = pubkey)
        )
            .onSuccess { event ->
                val entity = VoteEntity(
                    id = event.id().toHex(),
                    postId = postId,
                    pubkey = event.author().toHex(),
                    isPositive = isPositive,
                    createdAt = event.createdAt().secs(),
                )
                voteUpsertDao.upsertVote(voteEntity = entity)
            }
            .onFailure {
                Log.w(tag, "Failed to publish vote: ${it.message}", it)
            }
    }
}
