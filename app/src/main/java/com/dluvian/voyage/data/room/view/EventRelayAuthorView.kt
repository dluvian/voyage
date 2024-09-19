package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl


@DatabaseView(
    "SELECT rootPost.pubkey, rootPost.relayUrl, COUNT(*) AS relayCount " +
            "FROM rootPost " +
            "GROUP BY rootPost.pubkey, rootPost.relayUrl"
)
data class EventRelayAuthorView(
    val pubkey: PubkeyHex,
    val relayUrl: RelayUrl,
    val relayCount: Int
)
