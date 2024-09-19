package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

@DatabaseView(
    "SELECT rootPost.id, " +
            "rootPost.pubkey, " +
            "rootPost.subject, " +
            "rootPost.content, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = rootPost.pubkey)) AS authorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = rootPost.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = rootPost.pubkey)) AS authorIsTrusted, " +
            "(SELECT EXISTS(SELECT * FROM mute WHERE mute.mutedItem = rootPost.pubkey AND mute.tag IS 'p')) AS authorIsMuted, " +
            "(SELECT EXISTS(SELECT * FROM profileSetItem WHERE profileSetItem.pubkey = rootPost.pubkey)) AS authorIsInList, " +
            "(SELECT EXISTS(SELECT * FROM lock WHERE lock.pubkey = rootPost.pubkey)) AS authorIsLocked " +
            "FROM rootPost "
)
data class SimplePostView(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val subject: String?,
    val content: String,
    val authorIsOneself: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
    val authorIsMuted: Boolean,
    val authorIsInList: Boolean,
    val authorIsLocked: Boolean,
)
