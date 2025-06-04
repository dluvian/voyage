package com.dluvian.voyage.model

import rust.nostr.sdk.PublicKey

sealed class TrustProfile(open val pubkey: PublicKey, private var name: String) {
    fun uiName(): String {
        return if (name.isNotBlank()) name else pubkey.toHex()
    }

    fun setName(name: String) {
        this.name = name
    }
}

class OneselfProfile(override val pubkey: PublicKey, var name: String) :
    TrustProfile(pubkey = pubkey, name = name)

class FollowedProfile(override val pubkey: PublicKey, var name: String) :
    TrustProfile(pubkey = pubkey, name = name)

class TrustedProfile(override val pubkey: PublicKey, var name: String) :
    TrustProfile(pubkey = pubkey, name = name)

class UnknownProfile(override val pubkey: PublicKey, var name: String = "") :
    TrustProfile(pubkey = pubkey, name = name)
