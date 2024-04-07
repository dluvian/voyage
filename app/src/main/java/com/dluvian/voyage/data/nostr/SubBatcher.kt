package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.createReplyAndVoteFilters
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.syncedPutOrAdd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.protocol.EventId
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SubBatcher"
private const val BATCH_DELAY = 2 * DEBOUNCE

class SubBatcher(private val subCreator: SubscriptionCreator) {
    private val idQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val votePubkeyQueue = mutableSetOf<PublicKey>()
    private val isProcessingSubs = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        startProcessingJob()
    }

    fun submitVotesAndReplies(
        relayUrl: RelayUrl,
        eventIds: List<EventIdHex>,
        votePubkeys: List<PublicKey>
    ) {
        if (eventIds.isEmpty()) return

        idQueue.syncedPutOrAdd(relayUrl, eventIds)
        synchronized(votePubkeyQueue) {
            if (votePubkeyQueue.isEmpty()) votePubkeyQueue.addAll(votePubkeys)
        }
        startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingSubs.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launchIO {
            while (true) {
                delay(BATCH_DELAY)

                val idsByRelay = mutableMapOf<RelayUrl, Set<EventIdHex>>()
                val votePubkeys = mutableListOf<PublicKey>()
                synchronized(idQueue) {
                    idsByRelay.putAll(idQueue)
                    idQueue.clear()
                }
                synchronized(votePubkeyQueue) {
                    votePubkeys.addAll(votePubkeyQueue)
                    votePubkeyQueue.clear()
                }

                val timestamp = Timestamp.now()
                val convertedIds = mutableMapOf<EventIdHex, EventId>()
                idsByRelay.forEach { (relay, ids) ->
                    val eventIds = ids.map {
                        val id = convertedIds[it] ?: EventId.fromHex(it)
                        convertedIds.putIfAbsent(it, id) ?: id
                    }
                    val filters = createReplyAndVoteFilters(
                        ids = eventIds,
                        votePubkeys = votePubkeys,
                        timestamp = timestamp
                    )
                    Log.d(TAG, "Sub ${ids.size} ids and ${votePubkeys.size} pubkeys in $relay")
                    subCreator.subscribe(relayUrl = relay, filters = filters)
                }

            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingSubs.set(false)
        }
    }
}
