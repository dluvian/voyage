package com.dluvian.voyage.nostr

class Subscriber(private val service: NostrService) {
    suspend fun unsubAll() {
        service.unsubAll()
    }

    suspend fun subMyData() {
        TODO()
    }

    suspend fun subBookmarks() {
        TODO()
    }
}