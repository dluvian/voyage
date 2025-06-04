package com.dluvian.voyage.nostr

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.AdmitPolicy
import rust.nostr.sdk.AdmitStatus
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId

class NostrAdmission() : AdmitPolicy {
    val mutex = Mutex()
    val dbIds = mutableSetOf<EventId>()

    override suspend fun admitConnection(relayUrl: String): AdmitStatus {
        // Add more logic after supporting (temporary) relay blacklists
        return AdmitStatus.Success
    }

    override suspend fun admitEvent(
        relayUrl: String,
        subscriptionId: String,
        event: Event
    ): AdmitStatus {
        val alreadyInDb = mutex.withLock { dbIds.contains(event.id()) }
        if (alreadyInDb) return AdmitStatus.Rejected("Already in database")
        // Outdated replaceables don't trigger notification and can therefore be ignored
        // See: https://github.com/rust-nostr/nostr/pull/911

        // No need to verify ID and check against subscription filter
        // https://github.com/rust-nostr/nostr/issues/909#issuecomment-2933648117

        return AdmitStatus.Success
    }

    // Add IDs after querying db to prevent rechecking IDs on admission
    suspend fun addDatabaseIds(ids: Collection<EventId>) {
        mutex.withLock {
            dbIds.addAll(ids)
        }
    }
}
