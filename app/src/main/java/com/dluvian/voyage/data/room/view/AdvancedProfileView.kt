package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.shortenBech32


@DatabaseView(
    "SELECT *, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = profile.pubkey)) AS isFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = profile.pubkey)) AS isWebOfTrust, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = profile.pubkey)) AS isMe " +
            "FROM profile "
)
data class AdvancedProfileView(
    val pubkey: PubkeyHex = "",
    val name: String = pubkey.shortenBech32(),
    val isFriend: Boolean = false,
    val isWebOfTrust: Boolean = false,
    val isMe: Boolean = false,
)
