package com.dluvian.voyage.data.event

import com.dluvian.voyage.core.EventIdHex

class EventCacheClearer(
    private val syncedEventQueue: MutableSet<ValidatedEvent>,
    private val syncedIdCache: MutableSet<EventIdHex>,
) {
    fun clear() {
        synchronized(syncedEventQueue) {
            syncedEventQueue.clear()
        }
        synchronized(syncedIdCache) {
            syncedIdCache.clear()
        }
    }
}
