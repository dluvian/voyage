package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.RelayedValidatedEvent

class EventCacheClearer(
    private val syncedEventQueue: MutableSet<RelayedValidatedEvent>,
    private val syncedIdCache: MutableSet<EventIdHex>,
    private val syncedEventRelayCache: MutableSet<Pair<EventIdHex, RelayUrl>>,
) {
    fun clear() {
        synchronized(syncedEventQueue) {
            syncedEventQueue.clear()
        }
        synchronized(syncedIdCache) {
            syncedIdCache.clear()
        }
        synchronized(syncedEventRelayCache) {
            syncedEventRelayCache.clear()
        }
    }
}
