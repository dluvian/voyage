package com.dluvian.voyage.preferences

import rust.nostr.sdk.Kind

internal fun parseKinds(str: String): List<Kind> {
    return str.split(",")
        .mapNotNull { it.trim().toUIntOrNull() }
        .map { TODO("Wait for Kind.parseFromUint") }
}

internal fun kindsToString(kinds: List<Kind>): String {
    return kinds.map { it.asU16().toString() }.joinToString(separator = ",")
}
