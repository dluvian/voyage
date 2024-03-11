package com.dluvian.voyage.data.keys

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent

private const val TAG = "AccountKeyManager"
private const val NOSTR_SIGNER_PREFIX = "nostrsigner:"

class AccountKeyManager(
    private val context: Context,
    private val mnemonicManager: MnemonicManager,
    private val accountDao: AccountDao,
) : IPubkeyProvider {
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        ensureDataIntegrity()
    }

    override fun getPubkeyHex(): PubkeyHex {
        // TODO: cache pubkey
        return runBlocking {
            accountDao.getPubkey() ?: throw IllegalStateException("No pubkey set")
        }
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
            data = Uri.parse(NOSTR_SIGNER_PREFIX)
        }
        val infos = context.packageManager.queryIntentActivities(intent, 0)
        return infos.size > 0
    }

    private fun ensureDataIntegrity() {
        scope.launch {
            val missingAccount = accountDao.count() == 0
            if (!missingAccount) return@launch

            Log.w(TAG, "Database was missing account pubkey")
            val mnemonicPubkey = mnemonicManager.getMainAccountKeys().publicKey().toHex()
            accountDao.updateAccount(account = AccountEntity(pubkey = mnemonicPubkey))
        }
    }
}
