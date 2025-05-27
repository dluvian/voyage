package com.dluvian.voyage

import rust.nostr.sdk.AdmitPolicy
import rust.nostr.sdk.AdmitStatus
import rust.nostr.sdk.Event

class RelayChecker : AdmitPolicy {
    override suspend fun admitConnection(relayUrl: String): AdmitStatus {
        TODO("Not yet implemented")
    }

    override suspend fun admitEvent(
        relayUrl: String,
        subscriptionId: String,
        event: Event
    ): AdmitStatus {
        TODO("Not yet implemented")
    }
}