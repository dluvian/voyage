package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.utils.createReplyAndVoteFilters
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.syncedPutOrAdd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SubBatcher"
private const val BATCH_DELAY = 2 * DEBOUNCE

class SubBatcher(private val subCreator: SubscriptionCreator) {
    private val idQueue = mutableMapOf<RelayUrl, MutableSet<EventIdHex>>()
    private val isProcessingSubs = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        startProcessingJob()
    }

    fun submitVotesAndReplies(relayUrl: RelayUrl, eventIds: List<EventIdHex>) {
        if (eventIds.isEmpty()) return

        idQueue.syncedPutOrAdd(relayUrl, eventIds)
        startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingSubs.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launchIO {
            while (true) {
                delay(BATCH_DELAY)

                val idsByRelay = mutableMapOf<RelayUrl, Set<EventIdHex>>()
                synchronized(idQueue) {
                    idsByRelay.putAll(idQueue)
                    idQueue.clear()
                }

                val until = Timestamp.now()

                val replyAndVoteFilters = getReplyAndVoteFilters(
                    idsByRelay = idsByRelay,
                    until = until
                )
                replyAndVoteFilters.forEach { (relay, filters) ->
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
        idsByRelay: Map<RelayUrl, Set<EventIdHex>>,
        until: Timestamp,
    ): Map<RelayUrl, List<Filter>> {
        val convertedIds = mutableMapOf<EventIdHex, EventId>()

        return idsByRelay.mapValues { (_, ids) ->
            val eventIds = ids.map {
                val id = convertedIds[it] ?: EventId.fromHex(it)
                convertedIds.putIfAbsent(it, id) ?: id
            }
            createReplyAndVoteFilters(ids = eventIds, until = until)
        }
    }
}
