package com.dluvian.voyage.data

import com.dluvian.nostr_kt.RelayUrl
import rust.nostr.protocol.Event


data class RelayedEvent(val relayUrl: RelayUrl, val event: Event)
