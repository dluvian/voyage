package com.dluvian.voyage.provider

import com.dluvian.voyage.NostrService
import rust.nostr.sdk.EventId

class BookmarkProvider(private val service: NostrService) {
    private val bookmarks = mutableSetOf<EventId>()


    fun bookmarks() = bookmarks.toSet()
}