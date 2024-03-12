package com.dluvian.voyage.data.signer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rust.nostr.protocol.Event
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.UnsignedEvent
import java.util.concurrent.atomic.AtomicBoolean


class AccountManager(
    private val mnemonicSigner: ISigner,
    private val externalSigner: ISigner,
    private val accountDao: AccountDao
) : IPubkeyProvider, IAccountSwitcher {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val tag = "AccountManager"

    override val accountType: MutableState<AccountType>

    init {
        val dbPubkey = runBlocking { accountDao.getMyPubkey() }
        if (dbPubkey == null) {
            Log.i(tag, "No acc pubkey found in database. Initialize new.")
            val pubkeyHex = mnemonicSigner.tryGetPubkeyHex().getOrNull()
                ?: throw IllegalStateException("No signers initialized")
            accountType = mutableStateOf(DefaultAccount(PublicKey.fromHex(hex = pubkeyHex)))
            val account = AccountEntity(pubkey = pubkeyHex)
            scope.launch {
                accountDao.updateAccount(account = account)
            }.invokeOnCompletion {
                if (it != null) Log.w(tag, "Failed to save new acc pubkey $pubkeyHex in database")
                else Log.i(tag, "Successfully saved new acc pubkey $pubkeyHex in database")
            }
        } else {
            val publicKey = PublicKey.fromHex(dbPubkey)
            val account =
                if (dbPubkey == mnemonicSigner.getPubkeyHex()) DefaultAccount(publicKey = publicKey)
                else ExternalAccount(publicKey = publicKey)
            accountType = mutableStateOf(account)
        }
    }

    override fun tryGetPubkeyHex(): Result<PubkeyHex> {
        return Result.success(accountType.value.publicKey.toHex())
    }

    private val isSwitchingAccount = AtomicBoolean(false)
    override fun useDefaultAccount() {
        Log.i(tag, "Use default account")
        if (accountType.value is DefaultAccount) return

        if (isSwitchingAccount.compareAndSet(false, true)) {
            val defaultPubkey = mnemonicSigner.getPubkeyHex()
            scope.launch {
                accountDao.updateAccount(account = AccountEntity(pubkey = defaultPubkey))
            }.invokeOnCompletion {
                if (it != null) Log.w(tag, "Failed to switch to default account")
                else {
                    Log.i(tag, "Switched to default account $defaultPubkey")
                    accountType.value = DefaultAccount(publicKey = PublicKey.fromHex(defaultPubkey))
                }
                isSwitchingAccount.set(false)
            }
        }
    }

    override fun useExternalAccount(): Boolean {
        Log.i(tag, "Use external account ${accountType.value}")
        if (accountType.value is ExternalAccount) return false

        if (isSwitchingAccount.compareAndSet(false, true)) {
            val externalPubkey = externalSigner.tryGetPubkeyHex()
                .onFailure { Log.w(tag, "Failed to retrieve external pubkey", it) }
                .getOrNull()
            if (externalPubkey == null) {
                isSwitchingAccount.set(false)
                return false
            }
            scope.launch {
                accountDao.updateAccount(account = AccountEntity(pubkey = externalPubkey))
            }.invokeOnCompletion {
                if (it != null) Log.w(tag, "Failed to switch to external account")
                else {
                    Log.i(tag, "Switched to external account $externalPubkey")
                    accountType.value = ExternalAccount(PublicKey.fromHex(externalPubkey))
                }
                isSwitchingAccount.set(false)
            }
        }

        return true
    }

    override fun isExternalSignerInstalled(context: Context): Boolean {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("nostrsigner:")
        }
        val infos = context.packageManager.queryIntentActivities(intent, 0)
        return infos.size > 0
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
