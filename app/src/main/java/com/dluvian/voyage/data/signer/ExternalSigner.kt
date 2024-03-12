package com.dluvian.voyage.data.signer

import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

class ExternalSigner : ISigner {
    override fun tryGetPubkeyHex(): Result<PubkeyHex> {
        return Result.failure(IllegalStateException("External signer is not implemented yet"))
    }

    override fun sign(unsignedEvent: UnsignedEvent): Result<Event> {
        return Result.failure(IllegalStateException("External signer is not implemented yet"))
    }
}
