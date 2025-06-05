package com.dluvian.voyage.model

import rust.nostr.sdk.PublicKey

sealed class SignerType(open val pubkey: PublicKey)
data class MnemonicSigner(override val pubkey: PublicKey) : SignerType(pubkey = pubkey)
data class NsecSigner(override val pubkey: PublicKey) : SignerType(pubkey = pubkey)
data class BunkerSigner(override val pubkey: PublicKey) : SignerType(pubkey = pubkey)
