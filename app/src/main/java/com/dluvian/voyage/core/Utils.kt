package com.dluvian.voyage.core

import rust.nostr.protocol.PublicKey

fun PublicKey.shortenedBech32(): String {
    val bech32 = this.toBech32()
    return "${bech32.take(10)}:${bech32.takeLast(5)}"
}
