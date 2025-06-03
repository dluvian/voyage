package com.dluvian.voyage.nostr

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.AdmitPolicy
import rust.nostr.sdk.AdmitStatus
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId

class NostrAdmission() : AdmitPolicy {
    val mutex = Mutex()
    val validatedIds = mutableSetOf<EventId>()

    override suspend fun admitConnection(relayUrl: String): AdmitStatus {
        // Add more logic after supporting (temporary) relay blacklists
        return AdmitStatus.Success
    }

    override suspend fun admitEvent(
        relayUrl: String,
        subscriptionId: String,
        event: Event
    ): AdmitStatus {
        val alreadyValidated = mutex.withLock { validatedIds.contains(event.id()) }
        if (alreadyValidated) return AdmitStatus.Rejected("Already handled")
        if (!event.verify()) return AdmitStatus.Rejected("Invalid event")

        mutex.withLock { validatedIds.add(event.id()) }

        return AdmitStatus.Success
    }

    // Add IDs after querying db to prevent re-verifying IDs on admission
    suspend fun addValidatedIds(ids: Collection<EventId>) {
        mutex.withLock {
            validatedIds.addAll(ids)
        }
    }
}