package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex


@DatabaseView(
    "SELECT post.pubkey, eventRelay.relayUrl AS relay, COUNT(*) AS relayCount " +
            "FROM eventRelay " +
            "LEFT JOIN post ON post.id = eventRelay.eventId " +
            "GROUP BY post.pubkey, eventRelay.relayUrl"
)
data class EventRelayAuthorView(
    val pubkey: PubkeyHex,
    val relay: RelayUrl,
    val relayCount: Int
)
