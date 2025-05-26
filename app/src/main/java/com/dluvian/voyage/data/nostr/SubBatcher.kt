package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.replyKinds
import com.dluvian.voyage.core.utils.syncedPutOrAdd
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SubBatcher"
private const val BATCH_DELAY = 2 * DEBOUNCE

// TODO: Not needed anymore? Since each REQ has a single filter
// TODO: This feels bad -> go FIX
class SubBatcher(
    private val subCreator: SubscriptionCreator,
    private val myPubkeyProvider: IMyPubkeyProvider
) {
    private val idMyVoteQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val idReplyQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val isProcessingSubs = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        startProcessingJob()
    }

    fun submitMyVotes(relayUrl: RelayUrl, eventIds: List<EventIdHex>) {
        if (eventIds.isEmpty()) return

        idMyVoteQueue.syncedPutOrAdd(relayUrl, eventIds)
        startProcessingJob()
    }

    fun submitReplies(relayUrl: RelayUrl, eventIds: List<EventIdHex>) {
        if (eventIds.isEmpty()) return

        idReplyQueue.syncedPutOrAdd(relayUrl, eventIds)
        startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingSubs.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launchIO {
            while (true) {
                delay(BATCH_DELAY)

                val voteIdsByRelay = mutableMapOf<RelayUrl, Set<EventIdHex>>()
                synchronized(idMyVoteQueue) {
                    voteIdsByRelay.putAll(idMyVoteQueue)
                    idMyVoteQueue.clear()
                }

                val replyIdsByRelay = mutableMapOf<RelayUrl, Set<EventIdHex>>()
                synchronized(idReplyQueue) {
                    replyIdsByRelay.putAll(idReplyQueue)
                    idReplyQueue.clear()
                }

                val until = Timestamp.now()

                getReplyAndMyVoteFilters(
                    voteIdsByRelay = voteIdsByRelay,
                    replyIdsByRelay = replyIdsByRelay,
                    until = until
                ).forEach { (relay, filters) ->
                    Log.d(TAG, "Sub ${filters.size} filters in $relay")
                    subCreator.subscribeMany(relayUrl = relay, filters = filters)
                }
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingSubs.set(false)
        }
    }

    private fun getReplyAndMyVoteFilters(
        voteIdsByRelay: Map<RelayUrl, Set<EventIdHex>>,
        replyIdsByRelay: Map<RelayUrl, Set<EventIdHex>>,
        until: Timestamp,
    ): Map<RelayUrl, List<Filter>> {
        val convertedIds = mutableMapOf<EventIdHex, EventId>()
        val allRelays = voteIdsByRelay.keys + replyIdsByRelay.keys

        return allRelays.associateWith { relay ->
            val voteIds = voteIdsByRelay.getOrDefault(relay, emptySet())
                .map { convertedIds.mapCachedEventId(hex = it) }
            val replyIds = replyIdsByRelay.getOrDefault(relay, emptySet())
                .map { convertedIds.mapCachedEventId(hex = it) }

            val voteFilter = voteIds.let { ids ->
                if (ids.isEmpty()) null
                else FilterCreator.createReactionaryFilter(
                    ids = ids,
                    kinds = listOf(Kind.fromStd(KindStandard.REACTION)),
                    until = until
                ).author(myPubkeyProvider.getPublicKey())
            }
            val replyFilter = replyIds.minus(voteIds).let { ids ->
                if (ids.isEmpty()) null
                else FilterCreator.createReactionaryFilter(
                    ids = ids,
                    kinds = replyKinds,
                    until = until
                )
            }

            listOfNotNull(voteFilter, replyFilter)
        }
    }
}

private fun MutableMap<EventIdHex, EventId>.mapCachedEventId(hex: PubkeyHex): EventId {
    val id = this[hex] ?: EventId.parse(hex)
    return this.putIfAbsent(hex, id) ?: id
}
