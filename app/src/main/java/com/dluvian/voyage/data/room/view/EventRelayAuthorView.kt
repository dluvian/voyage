package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex


@DatabaseView(
    "SELECT post.pubkey, postRelay.relayUrl AS relay, COUNT(*) AS relayCount " +
            "FROM postRelay " +
            "LEFT JOIN post ON post.id = postRelay.postId " +
            "GROUP BY post.pubkey, postRelay.relayUrl"
)
data class EventRelayAuthorView(
    val pubkey: PubkeyHex,
    val relay: RelayUrl,
    val relayCount: Int
)
