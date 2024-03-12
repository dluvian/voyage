package com.dluvian.voyage.data.signer

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rust.nostr.protocol.Event
import rust.nostr.protocol.UnsignedEvent


class AccountManager(
    private val mnemonicSigner: ISigner,
    private val externalSigner: ISigner,
    private val accountDao: AccountDao
) : IPubkeyProvider {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val tag = "AccountManager"

    private val pubkey: MutableState<PubkeyHex>

    init {
        val dbPubkey = runBlocking { accountDao.getMyPubkey() }
        if (dbPubkey == null) {
            Log.i(tag, "No acc pubkey found in database. Initialize new.")
            val pubkeyHex = mnemonicSigner.tryGetPubkeyHex().getOrNull()
                ?: throw IllegalStateException("No signers initialized")
            pubkey = mutableStateOf(pubkeyHex)
            val account = AccountEntity(pubkey = pubkeyHex)
            scope.launch {
                accountDao.updateAccount(account = account)
            }.invokeOnCompletion {
                if (it != null) Log.w(tag, "Failed to save new acc pubkey $pubkeyHex in database")
                else Log.i(tag, "Successfully saved new acc pubkey $pubkeyHex in database")
            }
        } else pubkey = mutableStateOf(dbPubkey)
    }

    override fun tryGetPubkeyHex(): Result<PubkeyHex> {
        return Result.success(pubkey.value)
    }

    fun sign(unsignedEvent: UnsignedEvent): Result<Event> {
        val author = unsignedEvent.author().toHex()
        return when (author) {
            mnemonicSigner.tryGetPubkeyHex().getOrNull() -> {
                mnemonicSigner.sign(unsignedEvent = unsignedEvent)
            }

            externalSigner.tryGetPubkeyHex().getOrNull() -> {
                externalSigner.sign(unsignedEvent = unsignedEvent)
            }

            else -> Result.failure(IllegalStateException("You're not signed in correctly"))
        }
    }
}
