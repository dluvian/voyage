package com.dluvian.voyage.provider

import rust.nostr.sdk.Event

interface IEventUpdate {
    suspend fun update(event: Event)
}
