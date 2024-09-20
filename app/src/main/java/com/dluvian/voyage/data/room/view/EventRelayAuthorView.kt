package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl


@DatabaseView(
    "SELECT mainEvent.pubkey, mainEvent.relayUrl, COUNT(*) AS relayCount " +
            "FROM mainEvent " +
            "GROUP BY mainEvent.pubkey, mainEvent.relayUrl"
)
data class EventRelayAuthorView(
    val pubkey: PubkeyHex,
    val relayUrl: RelayUrl,
    val relayCount: Int
)
