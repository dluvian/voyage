package com.dluvian.nostr_kt

import rust.nostr.protocol.Event

data class RelayedEvent(val relayUrl: RelayUrl, val event: Event)
