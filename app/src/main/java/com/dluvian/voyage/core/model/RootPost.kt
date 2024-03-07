package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

data class RootPost(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val topic: String,
    val timeStr: String,
    val title: String,
    val content: String,
    val myVote: Vote,
    val tally: Int,
    val ratioInPercent: Int,
    val commentCount: Int,
    val eventRelayCount: Int,
)
