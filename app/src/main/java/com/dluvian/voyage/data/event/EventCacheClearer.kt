package com.dluvian.voyage.data.event

import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.RelayedValidatedEvent

class EventCacheClearer(
    private val nostrClient: NostrClient,
    private val syncedEventQueue: MutableSet<RelayedValidatedEvent>,
    private val syncedIdCache: MutableSet<EventIdHex>,
    private val syncedPostRelayCache: MutableSet<Pair<EventIdHex, RelayUrl>>,
) {
    fun clear() {
        nostrClient.unsubscribeAll()
        synchronized(syncedEventQueue) {
            syncedEventQueue.clear()
        }
        synchronized(syncedIdCache) {
            syncedIdCache.clear()
        }
        synchronized(syncedPostRelayCache) {
            syncedPostRelayCache.clear()
        }
    }
}
