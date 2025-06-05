package com.dluvian.voyage

import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
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

fun Event.parentId(): EventId? {
    return when (this.kind().asStd()) {
        // TODO: Upstream nip10 and nip22 extract
        KindStandard.TEXT_NOTE -> this.tags().eventIds().firstOrNull()
        KindStandard.COMMENT -> this.tags().eventIds().firstOrNull()
        null -> null
        else -> this.tags().eventIds().firstOrNull()
    }
}
