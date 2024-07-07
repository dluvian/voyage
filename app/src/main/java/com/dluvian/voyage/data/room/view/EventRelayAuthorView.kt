package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.core.PubkeyHex


@DatabaseView(
    "SELECT post.pubkey, post.relayUrl, COUNT(*) AS relayCount " +
            "FROM post " +
            "GROUP BY post.pubkey, post.relayUrl"
)
data class EventRelayAuthorView(
    val pubkey: PubkeyHex,
    val relayUrl: RelayUrl,
    val relayCount: Int
)
