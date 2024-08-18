package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.nostr.RelayUrl

data class PostDetails(
    val id: EventIdHex,
    val firstSeenIn: RelayUrl,
    val json: String,
)
