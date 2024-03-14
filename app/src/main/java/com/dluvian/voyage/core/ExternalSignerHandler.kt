package com.dluvian.voyage.core

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

class ExternalSignerHandler(
    private val requestExternalAccountLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val requestVoteSignatureLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    private val voteSignature = Channel<String?>()
    fun requestExternalAccount(): Throwable? {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))
            intent.putExtra("permissions", "[{\"type\":\"get_public_key\"}]")
            intent.putExtra("type", "get_public_key")
            requestExternalAccountLauncher.launch(intent)
        }.exceptionOrNull()
    }

    fun signVote(
        unsignedEvent: UnsignedEvent,
        packageName: String
    ): Result<Event> {
        val err = requestSignature(
            eventJson = unsignedEvent.asJson(),
            eventId = unsignedEvent.id().toHex(),
            npub = unsignedEvent.author().toBech32(),
            packageName = packageName,
            launcher = requestVoteSignatureLauncher
        )
        if (err != null) return Result.failure(IllegalStateException("Failed to request signature"))

        return runBlocking {
            val signature = async {
                voteSignature.receive()
            }.await()
            if (signature == null) Result.failure(IllegalStateException("Failed to retrieve signature"))
            else Result.success(unsignedEvent.addSignature(sig = signature))
        }
    }

    private fun requestSignature(
        eventJson: String,
        eventId: String,
        npub: String,
        packageName: String,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ): Throwable? {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:$eventJson"))
            intent.`package` = packageName
            intent.putExtra("type", "sign_event")
            intent.putExtra("id", eventId)
            intent.putExtra("current_user", npub)
            launcher.launch(intent)
        }.exceptionOrNull()
    }

    suspend fun processExternalVoteSignature(result: ActivityResult) {
        val signature = result.data?.getStringExtra("signature")
        voteSignature.send(signature)
    }
}
