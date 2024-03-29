package com.dluvian.voyage.data.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.data.event.EventCacheClearer
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.ResetDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import rust.nostr.protocol.PublicKey

class AccountSwitcher(
    private val accountManager: AccountManager,
    private val accountDao: AccountDao,
    private val resetDao: ResetDao,
    private val eventCacheClearer: EventCacheClearer,
    private val nostrSubscriber: NostrSubscriber,
) {
    private val tag = "AccountSwitcher"

    val accountType: State<AccountType> = accountManager.accountType

    fun isExternalSignerInstalled(context: Context): Boolean {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("nostrsigner:")
        }
        val result = runCatching { context.packageManager.queryIntentActivities(intent, 0) }
        return !result.getOrNull().isNullOrEmpty()
    }

    suspend fun useDefaultAccount() {
        if (accountManager.accountType.value is DefaultAccount) return
        Log.i(tag, "Use default account")

        val defaultPubkey = accountManager.mnemonicSigner.getPubkeyHex()
        updateAndReset(account = AccountEntity(pubkey = defaultPubkey))
        accountManager.accountType.value = DefaultAccount(
            publicKey = PublicKey.fromHex(hex = defaultPubkey)
        )
    }

    suspend fun useExternalAccount(publicKey: PublicKey, packageName: String) {
        if (accountManager.accountType.value is ExternalAccount) return
        Log.i(tag, "Use external account")

        val externalPubkey = publicKey.toHex()
        updateAndReset(account = AccountEntity(pubkey = externalPubkey, packageName = packageName))
        accountManager.accountType.value = ExternalAccount(publicKey = publicKey)
    }

    private suspend fun updateAndReset(account: AccountEntity) {
        Log.i(tag, "Update account and reset caches")
        eventCacheClearer.clear()
        accountDao.updateAccount(account = account)
        resetDao.resetAfterAccountChange()
        nostrSubscriber.subMyAccountAndTrustData()
    }
}