package com.dluvian.voyage.provider

import com.dluvian.voyage.NostrService
import rust.nostr.sdk.EventId

class BookmarkProvider(private val service: NostrService) {
    private val bookmarks = mutableSetOf<EventId>()
    // TODO: Switch signer
    // TODO: Listen to database events

    fun bookmarks() = bookmarks.toSet()
}