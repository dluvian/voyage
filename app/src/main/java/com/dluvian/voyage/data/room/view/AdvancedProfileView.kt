package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.SimpleNip19Profile
import com.dluvian.voyage.core.toShortenedBech32


@DatabaseView(
    "SELECT profile.pubkey, profile.name,  " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = profile.pubkey)) AS isFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = profile.pubkey)) AS isWebOfTrust, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = profile.pubkey)) AS isMe " +
            "FROM profile "
)
data class AdvancedProfileView(
    val pubkey: PubkeyHex = "",
    val name: String = pubkey.toShortenedBech32(),
    val isMe: Boolean = false,
    val isFriend: Boolean = false,
    val isWebOfTrust: Boolean = false,
) {
    fun toNip19(): SimpleNip19Profile {
        return SimpleNip19Profile(pubkey = pubkey)
    }
}
