package com.dluvian.voyage.provider

import rust.nostr.sdk.Client
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId

class BookmarkProvider(private val client: Client) : IProvider {
    private val bookmarks = mutableSetOf<EventId>()

    override suspend fun init() {
        TODO("Not yet implemented")
    }

    override suspend fun updateSigner() {
        TODO("Not yet implemented")
    }

    override suspend fun update(event: Event) {
        TODO("Not yet implemented")
    }

    fun bookmarks() = bookmarks.toSet()
}