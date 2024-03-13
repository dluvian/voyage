package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable
import rust.nostr.protocol.PublicKey

sealed class AccountType(open val publicKey: PublicKey)

@Immutable
data class DefaultAccount(override val publicKey: PublicKey) : AccountType(publicKey = publicKey)

@Immutable
data class ExternalAccount(override val publicKey: PublicKey) : AccountType(publicKey = publicKey)
