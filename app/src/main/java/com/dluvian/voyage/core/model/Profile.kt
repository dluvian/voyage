package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.shortenBech32

data class Profile(
    val pubkey: PubkeyHex,
    val name: String = pubkey.shortenBech32(),
    val about: String? = null,
    val picture: String? = null,
    val nip05: String? = null,
    val lud16: String? = null,
    val createdAt: Long,
)
