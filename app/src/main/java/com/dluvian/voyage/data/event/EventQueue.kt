package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.core.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.protocol.Event
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "EventQueue"
private const val EVENT_PROCESSING_DELAY = 600L

class EventQueue(
    private val syncedQueue: MutableSet<ValidatedEvent>,
    private val eventValidator: EventValidator,
    private val eventProcessor: EventProcessor,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val isProcessingEvents = AtomicBoolean(false)

    init {
        startProcessingJob()
    }

    fun submit(event: Event, subId: SubId, relayUrl: RelayUrl?) {
        if (relayUrl == null) {
            Log.w(TAG, "Unknown relay origin of eventId ${event.id().toHex()} of subId $subId")
            return
        }
        val submittableEvent = eventValidator.getValidatedEvent(
            event = event,
            subId = subId,
            relayUrl = relayUrl
        ) ?: return

        syncedQueue.add(submittableEvent)
        startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingEvents.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launchIO {
            while (true) {
                delay(EVENT_PROCESSING_DELAY)

                val events = mutableSetOf<ValidatedEvent>()
                synchronized(syncedQueue) {
                    events.addAll(syncedQueue.toList())
                    syncedQueue.clear()
                }
                eventProcessor.processEvents(events = events)
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingEvents.set(false)
        }
    }
}
