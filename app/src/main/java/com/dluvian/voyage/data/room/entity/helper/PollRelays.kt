package com.dluvian.voyage.data.room.entity.helper

import com.dluvian.voyage.data.nostr.RelayUrl

data class PollRelays(val relay1: RelayUrl?, val relay2: RelayUrl?) {
    fun toList() = listOfNotNull(relay1, relay2).distinct()
}
