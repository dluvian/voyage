package com.dluvian.nostr_kt

data class Nip65Relay(
    val url: RelayUrl,
    val isRead: Boolean = true,
    val isWrite: Boolean = true
)
