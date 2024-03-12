package com.dluvian.voyage.core.model

import rust.nostr.protocol.PublicKey

sealed class AccountType(open val publicKey: PublicKey)
data class DefaultAccount(override val publicKey: PublicKey) : AccountType(publicKey = publicKey)
data class ExternalAccount(override val publicKey: PublicKey) : AccountType(publicKey = publicKey)
