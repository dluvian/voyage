package com.dluvian.voyage.data.account

import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.protocol.PublicKey

interface IPubkeyProvider {
    fun getPublicKey(): PublicKey
    fun getPubkeyHex(): PubkeyHex {
        return getPublicKey().toHex()
    }
}
