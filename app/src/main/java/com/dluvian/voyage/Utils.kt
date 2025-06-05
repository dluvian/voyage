package com.dluvian.voyage

import rust.nostr.sdk.Event
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.TagKind

fun Event.isReply(): Boolean {
    val kindStd = this.kind().asStd()
    if (kindStd == KindStandard.COMMENT) return true
    if (kindStd == KindStandard.TEXT_NOTE && this.tags().eventIds().isNotEmpty()) return true

    return false
}

fun Event.subject(): String? {
    return this.tags().find(TagKind.Subject)?.content()?.trim()
}

