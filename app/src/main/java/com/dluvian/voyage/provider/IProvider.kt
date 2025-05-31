package com.dluvian.voyage.provider

import rust.nostr.sdk.Event

interface IProvider {
    suspend fun init()
    suspend fun updateSigner()
    suspend fun update(event: Event)
}
