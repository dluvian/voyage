package com.dluvian.voyage.core.model

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

interface IParentUI {
    val id: EventIdHex
    val content: AnnotatedString
    val pubkey: PubkeyHex
    val trustType: TrustType
    val relayUrl: RelayUrl
    val replyCount: Int
    val createdAt: Long

    fun getRelevantId() = when (this) {
        is RootPostUI -> this.crossPostedId ?: this.id
        is ReplyUI -> this.id
        else -> this.id
    }

    fun getRelevantPubkey() = when (this) {
        is RootPostUI -> this.crossPostedPubkey ?: this.pubkey
        is ReplyUI -> this.pubkey
        else -> this.pubkey
    }
}
