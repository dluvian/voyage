package com.dluvian.voyage.data.account

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


class AccountManager(
    private val mnemonicSigner: MnemonicSigner,
    private val externalSigner: ExternalSigner,
    private val accountSwitcher: AccountSwitcher,
    private val accountDao: AccountDao,
    private val context: Context,
) : IPubkeyProvider {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val tag = "AccountManager"

    val accountType: MutableState<AccountType>

    init {
        val dbAccount = runBlocking { accountDao.getAccount() }
        if (dbAccount == null) {
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
            val publicKey = PublicKey.fromHex(dbAccount.pubkey)
            val account = if (dbAccount.packageName == null) DefaultAccount(publicKey = publicKey)
            else ExternalAccount(publicKey = publicKey)
            accountType = mutableStateOf(account)
        }
    }

    override fun tryGetPubkeyHex(): Result<PubkeyHex> {
        return Result.success(accountType.value.publicKey.toHex())
    }

    suspend fun useDefaultAccount() {
        if (accountType.value is DefaultAccount) return
        Log.i(tag, "Use default account")

        val defaultPubkey = accountSwitcher.useDefaultAccount()
        accountType.value = DefaultAccount(publicKey = PublicKey.fromHex(hex = defaultPubkey))
    }

    suspend fun useExternalAccount(publicKey: PublicKey, packageName: String) {
        if (accountType.value is ExternalAccount) return
        Log.i(tag, "Use external account")

        accountSwitcher.useExternalAccount(publicKey, packageName)
        accountType.value = ExternalAccount(publicKey = publicKey)
    }

    fun isExternalSignerInstalled(context: Context): Boolean {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("nostrsigner:")
        }
        val result = runCatching { context.packageManager.queryIntentActivities(intent, 0) }
        return !result.getOrNull().isNullOrEmpty()
    }

    suspend fun sign(unsignedEvent: UnsignedEvent): Result<Event> {
        val author = unsignedEvent.author().toHex()
        return when (author) {
            mnemonicSigner.tryGetPubkeyHex().getOrNull() -> {
                mnemonicSigner.sign(unsignedEvent = unsignedEvent)
            }

            externalSigner.tryGetPubkeyHex().getOrNull() -> {
                externalSigner.sign(
                    unsignedEvent = unsignedEvent,
                    context = context,
                    packageName = accountDao.getPackageName()
                )
            }

            else -> Result.failure(IllegalStateException("You're not signed in correctly"))
        }
    }
}
