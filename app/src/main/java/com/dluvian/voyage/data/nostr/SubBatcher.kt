package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.reactionKind
import com.dluvian.voyage.core.utils.reactionaryKinds
import com.dluvian.voyage.core.utils.replyKinds
import com.dluvian.voyage.core.utils.syncedPutOrAdd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SubBatcher"
private const val BATCH_DELAY = 2 * DEBOUNCE

class SubBatcher(private val subCreator: SubscriptionCreator) {
    private val idVoteQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val idReplyQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val isProcessingSubs = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        startProcessingJob()
    }

    fun submitVotes(relayUrl: RelayUrl, eventIds: List<EventIdHex>) {
        if (eventIds.isEmpty()) return

        idVoteQueue.syncedPutOrAdd(relayUrl, eventIds)
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
                synchronized(idVoteQueue) {
                    voteIdsByRelay.putAll(idVoteQueue)
                    idVoteQueue.clear()
                }

                val replyIdsByRelay = mutableMapOf<RelayUrl, Set<EventIdHex>>()
                synchronized(idReplyQueue) {
                    replyIdsByRelay.putAll(idReplyQueue)
                    idReplyQueue.clear()
                }

                val until = Timestamp.now()

                getReplyAndVoteFilters(
                    voteIdsByRelay = voteIdsByRelay,
                    replyIdsByRelay = replyIdsByRelay,
                    until = until
                ).forEach { (relay, filters) ->
                    Log.d(TAG, "Sub ${filters.size} filters in $relay")
                    subCreator.subscribe(relayUrl = relay, filters = filters)
                }
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingSubs.set(false)
        }
    }

    private fun getReplyAndVoteFilters(
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

            val combinedFilter = FilterCreator.createReactionaryFilter(
                ids = voteIds.intersect(replyIds).toList(),
                kinds = reactionaryKinds,
                until = until
            )
            val voteOnlyFilter = voteIds.minus(replyIds).let { ids ->
                if (ids.isEmpty()) null
                else FilterCreator.createReactionaryFilter(
                    ids = ids,
                    kinds = listOf(reactionKind),
                    until = until
                )
            }
            val replyOnlyFilter = replyIds.minus(voteIds).let { ids ->
                if (ids.isEmpty()) null
                else FilterCreator.createReactionaryFilter(
                    ids = ids,
                    kinds = replyKinds,
                    until = until
                )
            }

            listOfNotNull(combinedFilter, voteOnlyFilter, replyOnlyFilter)
        }
    }
}

private fun MutableMap<EventIdHex, EventId>.mapCachedEventId(hex: PubkeyHex): EventId {
    val id = this[hex] ?: EventId.fromHex(hex)
    return this.putIfAbsent(hex, id) ?: id
}
