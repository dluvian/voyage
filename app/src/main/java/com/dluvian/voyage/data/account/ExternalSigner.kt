package com.dluvian.voyage.data.account

import android.util.Log
import com.dluvian.voyage.core.ExternalSignerHandler
import rust.nostr.sdk.Event
import rust.nostr.sdk.UnsignedEvent

private const val TAG = "ExternalSigner"

class ExternalSigner(private val handler: ExternalSignerHandler) {
    suspend fun sign(
        unsignedEvent: UnsignedEvent,
        packageName: String
    ): Result<Event> {
        return handler.sign(
            unsignedEvent = unsignedEvent,
            packageName = packageName
        ).onFailure { Log.w(TAG, "Failed to sign event", it) }
    }
}
