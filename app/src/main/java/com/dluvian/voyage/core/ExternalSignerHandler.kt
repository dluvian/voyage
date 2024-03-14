package com.dluvian.voyage.core

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityOptionsCompat
import kotlinx.coroutines.channels.Channel
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

class ExternalSignerHandler(
    private val requestAccountLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val requestSignatureLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    private val signatureChannel = Channel<String?>()
    fun requestExternalAccount(): Throwable? {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))
            intent.putExtra("permissions", "[{\"type\":\"get_public_key\"}]")
            intent.putExtra("type", "get_public_key")
            requestAccountLauncher.launch(intent)
        }.exceptionOrNull()
    }

    suspend fun sign(
        unsignedEvent: UnsignedEvent,
        packageName: String
    ): Result<Event> {
        val err = runCatching {
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.parse("nostrsigner:${unsignedEvent.asJson()}")
            )
            intent.`package` = packageName
            intent.putExtra("type", "sign_event")
            intent.putExtra("id", unsignedEvent.id().toHex())
            intent.putExtra("current_user", unsignedEvent.author().toBech32())
            requestSignatureLauncher.launch(
                input = intent,
                options = ActivityOptionsCompat.makeBasic()
            )
        }.exceptionOrNull()
        if (err != null) return Result.failure(err)

        val signature = signatureChannel.receive()
        return if (signature == null) Result.failure(IllegalStateException("Failed to retrieve signature"))
        else Result.success(unsignedEvent.addSignature(sig = signature))
    }

    suspend fun processExternalSignature(result: ActivityResult) {
        val signature = result.data?.getStringExtra("signature")
        signatureChannel.send(signature)
    }
}
