package com.dluvian.voyage.data.event

import rust.nostr.sdk.EventId

class IdCacheClearer(
    private val syncedIdCache: MutableSet<EventId>,
) {
    fun clear() {
        synchronized(syncedIdCache) {
            syncedIdCache.clear()
        }
        // Don't clear eventQueue.
        // Queue would be lost if user quickly reopens the app
    }
}
