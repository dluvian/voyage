package com.dluvian.voyage.data.account

import android.util.Log
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.SignerLauncher
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

private const val TAG = "ExternalSigner"

class ExternalSigner(private val handler: ExternalSignerHandler) {
    suspend fun sign(
        signerLauncher: SignerLauncher,
        unsignedEvent: UnsignedEvent,
        packageName: String
    ): Result<Event> {
        return handler.sign(
            signerLauncher = signerLauncher,
            unsignedEvent = unsignedEvent,
            packageName = packageName
        ).onFailure { Log.w(TAG, "Failed to sign event", it) }
    }
}
