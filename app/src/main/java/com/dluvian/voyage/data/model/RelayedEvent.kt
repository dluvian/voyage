package com.dluvian.voyage.data.model

import com.dluvian.nostr_kt.RelayUrl

data class RelayedItem<T>(
    val item: T,
    val relayUrl: RelayUrl
)
