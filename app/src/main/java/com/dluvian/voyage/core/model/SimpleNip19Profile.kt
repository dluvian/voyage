package com.dluvian.voyage.core.model

import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex

data class SimpleNip19Profile(
    val pubkey: PubkeyHex,
    val relays: List<RelayUrl> = emptyList()
)
