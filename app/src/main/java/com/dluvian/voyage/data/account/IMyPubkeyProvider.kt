package com.dluvian.voyage.data.account

import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.sdk.PublicKey

interface IMyPubkeyProvider {
    fun getPublicKey(): PublicKey
    fun getPubkeyHex(): PubkeyHex {
        return getPublicKey().toHex()
    }
}
