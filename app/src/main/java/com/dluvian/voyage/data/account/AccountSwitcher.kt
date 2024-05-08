package com.dluvian.voyage.data.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import com.dluvian.nostr_kt.getCurrentSecs
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.FEED_PAGE_SIZE
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.data.event.IdCacheClearer
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import kotlinx.coroutines.delay
import rust.nostr.protocol.PublicKey

private const val TAG = "AccountSwitcher"

class AccountSwitcher(
    private val accountManager: AccountManager,
    private val accountDao: AccountDao,
    private val idCacheClearer: IdCacheClearer,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val nostrSubscriber: NostrSubscriber,
) {

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
        Log.i(TAG, "Use default account")

        val defaultPubkey = accountManager.mnemonicSigner.getPubkeyHex()
        updateAndReset(account = AccountEntity(pubkey = defaultPubkey))
        accountManager.accountType.value = DefaultAccount(
            publicKey = PublicKey.fromHex(hex = defaultPubkey)
        )
    }

    suspend fun useExternalAccount(publicKey: PublicKey, packageName: String) {
        if (accountManager.accountType.value is ExternalAccount) return
        Log.i(TAG, "Use external account")

        val externalPubkey = publicKey.toHex()
        updateAndReset(account = AccountEntity(pubkey = externalPubkey, packageName = packageName))
        accountManager.accountType.value = ExternalAccount(publicKey = publicKey)
    }

    private suspend fun updateAndReset(account: AccountEntity) {
        Log.i(TAG, "Update account and reset caches")
        lazyNostrSubscriber.subCreator.unsubAll()
        idCacheClearer.clear()
        accountDao.updateAccount(account = account)
        lazyNostrSubscriber.lazySubMyAccount()
        delay(DELAY_1SEC)
        nostrSubscriber.subFeed(
            until = getCurrentSecs(),
            limit = FEED_PAGE_SIZE,
            setting = HomeFeedSetting
        )
    }
}