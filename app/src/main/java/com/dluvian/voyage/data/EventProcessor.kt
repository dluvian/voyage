package com.dluvian.voyage.data

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.nostr_kt.isPost
import com.dluvian.nostr_kt.matches
import com.dluvian.voyage.data.model.RelayedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.protocol.Event
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

class EventProcessor(
    private val filterCache: Map<SubId, List<Filter>>
) {
    private val tag = "EventProcessor"
    private val eventProcessingDelay = 500L
    private val maxSqlParams = 210
    private val scope = CoroutineScope(Dispatchers.IO)

    // TODO: cache
//    private val eventIdCache = Collections.synchronizedSet(mutableSetOf<EventIdHex>())
    private var upperTimeBoundary = getUpperTimeBoundary()

    // Not a synchronized set bc we synchronize with `synchronized()`
    private val queue = mutableSetOf<RelayedEvent>()
    private val isProcessingEvents = AtomicBoolean(false)

    init {
        startProcessingJob()
    }

    fun submit(event: Event, subId: SubId, relayUrl: RelayUrl?) {
        if (relayUrl == null) {
            Log.w(tag, "Origin relay of ${event.id().toHex()} is unknown")
            return
        }
        if (isFromFuture(event)) {
            Log.w(tag, "Discard event from the future, ${event.id().toHex()} from $relayUrl")
            return
        }
        if (!matchesFilter(subId = subId, event = event)) {
            Log.w(tag, "Discard event not matching filter, ${event.id().toHex()} from $relayUrl")
            return
        }
        if (!isValid(event)) return

        synchronized(queue) {
            queue.add(RelayedEvent(event = event, relayUrl = relayUrl))
        }
        if (!isProcessingEvents.get()) startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingEvents.compareAndSet(false, true)) return
        Log.i(tag, "Start job")
        scope.launch {
            while (true) {
                delay(eventProcessingDelay)

                val items = mutableSetOf<RelayedEvent>()
                synchronized(queue) {
                    items.addAll(queue.toSet())
                    queue.clear()
                }
                processQueue(items = items)
            }
        }.invokeOnCompletion {
            Log.w(tag, "Processing job completed", it)
            isProcessingEvents.set(false)
        }
    }

    private fun processQueue(items: Set<RelayedEvent>) {
        if (items.isEmpty()) return
        Log.d(tag, "Process queue of ${items.size} events")

        val posts = mutableListOf<RelayedEvent>()

        items.forEach {
            if (it.event.isPost()) posts.add(it)
        }

        posts.chunked(maxSqlParams).forEach { processPosts(relayedEvents = it) }
    }

    private fun isValid(event: Event): Boolean {
        return event.isPost() && verify(event)
    }

    private fun processPosts(relayedEvents: Collection<RelayedEvent>) {
        Log.d(tag, "Process ${relayedEvents.size} posts")

        scope.launch {
            TODO("Insert post and eventRelay")
        }.invokeOnCompletion { exception ->
            if (exception != null) {
                Log.w(tag, "Failed to process posts", exception)
                return@invokeOnCompletion
            }
        }
    }

    private fun verify(event: Event): Boolean {
        val isValid = event.verify()
        if (!isValid) Log.w(tag, "Invalid event kind=${event.kind()} id=${event.id()} ")

        return isValid
    }

    private fun getUpperTimeBoundary(): Long {
        return Timestamp.now().asSecs().toLong() + 60
    }

    private fun isFromFuture(event: Event): Boolean {
        val createdAt = event.createdAt().asSecs().toLong()
        if (createdAt > upperTimeBoundary) {
            upperTimeBoundary = getUpperTimeBoundary()
            return createdAt > upperTimeBoundary
        }
        return false
    }

    private fun matchesFilter(subId: SubId, event: Event): Boolean {
        val cache = filterCache.getOrDefault(subId, emptyList()).toList()
        if (cache.isEmpty()) return false
        return cache.any { it.matches(event) }
    }
}