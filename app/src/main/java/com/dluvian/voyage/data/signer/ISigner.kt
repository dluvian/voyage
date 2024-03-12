package com.dluvian.voyage.data.signer

import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

interface ISigner : IPubkeyProvider {
    fun sign(unsignedEvent: UnsignedEvent): Result<Event>
}
