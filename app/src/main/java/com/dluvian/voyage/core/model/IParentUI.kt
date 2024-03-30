package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

interface IParentUI {
    val id: EventIdHex
    val content: String
    val pubkey: PubkeyHex
}
