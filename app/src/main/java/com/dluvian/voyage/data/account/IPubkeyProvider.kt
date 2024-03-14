package com.dluvian.voyage.data.account

import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.protocol.PublicKey

interface IPubkeyProvider {
    fun tryGetPubkeyHex(): Result<PubkeyHex>
    fun getPubkeyHex(): PubkeyHex {
        return tryGetPubkeyHex().getOrThrow()
    }

    fun getPublicKey(): PublicKey {
        return PublicKey.fromHex(hex = getPubkeyHex())
    }
}
