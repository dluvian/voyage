package com.dluvian.voyage.data.model

import rust.nostr.protocol.EventId
import rust.nostr.protocol.PublicKey

data class EventSubset(
    val id: EventId,
    val pubkey: PublicKey
)
