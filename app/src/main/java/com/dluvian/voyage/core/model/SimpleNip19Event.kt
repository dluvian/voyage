package com.dluvian.voyage.core.model

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex

data class SimpleNip19Event(
    val eventId: EventIdHex,
    val relays: List<RelayUrl> = emptyList()
)
