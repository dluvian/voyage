package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.EventIdHex
import rust.nostr.protocol.Filter

data class FilterWrapper(
    val filter: Filter,
    val e: List<EventIdHex> = emptyList()
)
