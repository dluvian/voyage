package com.dluvian.voyage.data.keys

import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.protocol.PublicKey

interface IPubkeyProvider {
    fun getPubkeyHex(): PubkeyHex
    fun getPublicKey(): PublicKey {
        return PublicKey.fromHex(getPubkeyHex())
    }
}
