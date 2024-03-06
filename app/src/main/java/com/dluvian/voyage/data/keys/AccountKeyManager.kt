package com.dluvian.voyage.data.keys

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.dao.AccountDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.Event
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.UnsignedEvent

class AccountKeyManager(
    accountDao: AccountDao,
    private val context: Context,
    private val mnemonicManager: MnemonicManager
) : IPubkeyProvider {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val myPubkeyFlow =
        accountDao.getPubkeyFlow().stateIn(scope, SharingStarted.Eagerly, null)

    override fun getPubkeyHex(): PubkeyHex {
        return myPubkeyFlow.value ?: throw IllegalStateException("No pubkey set")
    }

    fun getPubkey(): PublicKey {
        return PublicKey.fromHex(getPubkeyHex())
    }

    fun sign(unsignedEvent: UnsignedEvent): Result<Event> {
        val unsignedAuthor = unsignedEvent.author()
        if (unsignedAuthor.toHex() != getPubkeyHex()) {
            val msg = "Can't sign event from ${
                unsignedEvent.author().toHex()
            }, logged in as ${getPubkeyHex()}"
            return Result.failure(IllegalArgumentException(msg))
        }
        val mnemonicKeys = mnemonicManager.getMainAccountKeys()
        if (unsignedAuthor == mnemonicKeys.publicKey()) {
            return Result.success(unsignedEvent.sign(mnemonicKeys))
        }
        if (isExternalSignerInstalled()) {
            // TODO: Handle Amber
            val jsonResult = "amberino"
            val event = Event.fromJson(jsonResult)
            return Result.success(event)
        }

        return Result.failure(IllegalArgumentException("Can't sign event"))
    }

    private fun isExternalSignerInstalled(): Boolean {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("nostrsigner:")
        }
        val infos = context.packageManager.queryIntentActivities(intent, 0)
        return infos.size > 0
    }
}
