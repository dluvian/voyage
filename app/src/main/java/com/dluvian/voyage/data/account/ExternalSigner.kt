package com.dluvian.voyage.data.account

import android.util.Log
import com.dluvian.nostr_kt.Kind
import com.dluvian.voyage.core.ExternalSignerHandler
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

class ExternalSigner() {
    lateinit var externalSignerHandler: ExternalSignerHandler
    private val tag = "ExternalSigner"


    fun sign(
        unsignedEvent: UnsignedEvent,
        packageName: String
    ): Result<Event> {
        return when (val kind = unsignedEvent.kind()) {
            Kind.REACTION.toULong() -> {
                externalSignerHandler.signVote(
                    unsignedEvent = unsignedEvent,
                    packageName = packageName
                )
            }

            else -> {
                val msg = "External signing of kind $kind is not implemented yet"
                Result.failure(IllegalArgumentException(msg))
            }
        }.onFailure { Log.w(tag, "Failed to sign event", it) }
    }
}
