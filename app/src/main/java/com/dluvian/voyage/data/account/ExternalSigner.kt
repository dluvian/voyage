package com.dluvian.voyage.data.account

import android.util.Log
import com.dluvian.voyage.core.ExternalSignerHandler
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

class ExternalSigner {
    lateinit var externalSignerHandler: ExternalSignerHandler
    private val tag = "ExternalSigner"

    suspend fun sign(unsignedEvent: UnsignedEvent, packageName: String): Result<Event> {
        return externalSignerHandler.sign(
            unsignedEvent = unsignedEvent,
            packageName = packageName
        ).onFailure { Log.w(tag, "Failed to sign event", it) }
    }
}
