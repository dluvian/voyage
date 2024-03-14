package com.dluvian.voyage.data.signer

import android.content.Context
import android.net.Uri
import com.dluvian.voyage.core.PubkeyHex
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

class ExternalSigner : IPubkeyProvider {
    override fun tryGetPubkeyHex(): Result<PubkeyHex> {
        return Result.failure(IllegalStateException("External signer is not implemented yet"))
    }

    fun sign(unsignedEvent: UnsignedEvent, packageName: String, context: Context): Result<Event> {
        val eventJson = unsignedEvent.asJson()
        val npub = unsignedEvent.author().toBech32()

        val result = context.contentResolver.query(
            Uri.parse("content://$packageName.SIGN_EVENT"),
            arrayOf(eventJson, "", npub),
            "1",
            null,
            null
        ) ?: return Result.failure(IllegalStateException("Signer failed to sign"))

        return runCatching {
            result.moveToFirst()
            val index = result.getColumnIndex("signature")
            val signature = result.getString(index)
            result.close()
            unsignedEvent.addSignature(signature)
        }
    }
}
