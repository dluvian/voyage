package com.dluvian.voyage.core

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityOptionsCompat
import kotlinx.coroutines.channels.Channel
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

private const val PERMISSIONS = """
    [
        {"type":"get_public_key"},
        {"type":"sign_event","kind":0},
        {"type":"sign_event","kind":1},
        {"type":"sign_event","kind":3},
        {"type":"sign_event","kind":5},
        {"type":"sign_event","kind":6},
        {"type":"sign_event","kind":7},
        {"type":"sign_event","kind":16},
        {"type":"sign_event","kind":10000},
        {"type":"sign_event","kind":10002},
        {"type":"sign_event","kind":10003},
        {"type":"sign_event","kind":10004},
        {"type":"sign_event","kind":10006},
        {"type":"sign_event","kind":10015},
        {"type":"sign_event","kind":22242}
    ]
"""

class ExternalSignerHandler {
    private var signerLauncher: ManagedLauncher? = null
    private var reqAccountLauncher: ManagedLauncher? = null
    private val signatureChannel = Channel<String?>()

    fun setSignerLauncher(launcher: ManagedLauncher) {
        signerLauncher = launcher
    }

    fun setAccountLauncher(launcher: ManagedLauncher) {
        reqAccountLauncher = launcher
    }

    fun requestExternalAccount(): Throwable? {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))
            intent.putExtra("permissions", PERMISSIONS)
            intent.putExtra("type", "get_public_key")
            reqAccountLauncher?.launch(intent) ?: throw IllegalStateException("Signer is null")
        }.exceptionOrNull()
    }

    suspend fun sign(unsignedEvent: UnsignedEvent, packageName: String): Result<Event> {
        val err = runCatching {
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.parse("nostrsigner:${unsignedEvent.asJson()}")
            )
            intent.`package` = packageName
            intent.putExtra("type", "sign_event")
            intent.putExtra("id", unsignedEvent.id()?.toHex())
            intent.putExtra("current_user", unsignedEvent.author().toBech32())
            signerLauncher?.launch(
                input = intent,
                options = ActivityOptionsCompat.makeBasic()
            ) ?: throw IllegalStateException("Signer is null")
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
