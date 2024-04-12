package com.dluvian.voyage.data.event

import com.dluvian.voyage.core.EventIdHex

class IdCacheClearer(
    private val syncedIdCache: MutableSet<EventIdHex>,
) {
    fun clear() {
        synchronized(syncedIdCache) {
            syncedIdCache.clear()
        }
        // Don't clear eventQueue.
        // Queue would be lost if user quickly reopens the app
    }
}
