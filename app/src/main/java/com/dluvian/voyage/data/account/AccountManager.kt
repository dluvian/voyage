package com.dluvian.voyage.data.account

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    val mnemonicSigner: MnemonicSigner,
    private val externalSigner: ExternalSigner,
    private val accountDao: AccountDao,
) : IPubkeyProvider {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val tag = "AccountManager"

    val accountType: MutableState<AccountType>

    init {
        val dbAccount = runBlocking { accountDao.getAccount() }
        if (dbAccount == null) {
            Log.i(tag, "No acc pubkey found in database. Initialize new.")
            val pubkey = mnemonicSigner.getPublicKey()
            val hex = pubkey.toHex()
            accountType = mutableStateOf(DefaultAccount(pubkey))
            val account = AccountEntity(pubkey = hex)
            scope.launch {
                accountDao.updateAccount(account = account)
            }.invokeOnCompletion {
                if (it != null) Log.w(tag, "Failed to save new acc pubkey $hex in database")
                else Log.i(tag, "Successfully saved new acc pubkey $hex in database")
            }
        } else {
            val publicKey = PublicKey.fromHex(dbAccount.pubkey)
            val account = if (dbAccount.packageName == null) DefaultAccount(publicKey = publicKey)
            else ExternalAccount(publicKey = publicKey)
            accountType = mutableStateOf(account)
        }
    }

    override fun getPublicKey(): PublicKey {
        return accountType.value.publicKey
    }

    suspend fun sign(unsignedEvent: UnsignedEvent): Result<Event> {
        return when (accountType.value) {
            is DefaultAccount -> {
                mnemonicSigner.sign(unsignedEvent = unsignedEvent)
            }

            is ExternalAccount -> {
                externalSigner.sign(
                    unsignedEvent = unsignedEvent,
                    packageName = accountDao.getPackageName()
                )
                    .onSuccess {
                        Log.i(
                            tag,
                            "Externally signed event of kind ${unsignedEvent.kind()}"
                        )
                    }
                    .onFailure {
                        Log.w(
                            tag,
                            "Failed to externally sign event of kind ${unsignedEvent.kind()}"
                        )
                    }
            }
        }
    }
}
