package com.dluvian.voyage.data.room.view

import androidx.room.DatabaseView
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

@DatabaseView(
    "SELECT post.id, " +
            "post.pubkey, " +
            "post.subject, " +
            "post.content, " +
            "(SELECT EXISTS(SELECT * FROM account WHERE account.pubkey = post.pubkey)) AS authorIsOneself, " +
            "(SELECT EXISTS(SELECT * FROM friend WHERE friend.friendPubkey = post.pubkey)) AS authorIsFriend, " +
            "(SELECT EXISTS(SELECT * FROM weboftrust WHERE weboftrust.webOfTrustPubkey = post.pubkey)) AS authorIsTrusted " +
            "FROM post " +
            "WHERE crossPostedId IS NULL AND crossPostedPubkey IS NULL"
)
data class SimplePostView(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val subject: String?,
    val content: String,
    val authorIsOneself: Boolean,
    val authorIsFriend: Boolean,
    val authorIsTrusted: Boolean,
)
