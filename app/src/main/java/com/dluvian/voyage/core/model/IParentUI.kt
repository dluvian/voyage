package com.dluvian.voyage.core.model

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex

interface IParentUI {
    val id: EventIdHex
    val content: AnnotatedString
    val pubkey: PubkeyHex
}
