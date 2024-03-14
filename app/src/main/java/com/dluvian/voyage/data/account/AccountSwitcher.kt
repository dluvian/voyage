package com.dluvian.voyage.data.account

import android.util.Log
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.EventCacheClearer
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.ResetDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import rust.nostr.protocol.PublicKey

class AccountSwitcher(
    private val mnemonicSigner: MnemonicSigner,
    private val accountDao: AccountDao,
    private val resetDao: ResetDao,
    private val eventCacheClearer: EventCacheClearer,
) {
    private val tag = "AccountSwitcher"

    suspend fun useDefaultAccount(): PubkeyHex {
        val defaultPubkey = mnemonicSigner.getPubkeyHex()
        updateAndReset(account = AccountEntity(pubkey = defaultPubkey))

        return defaultPubkey
    }

    suspend fun useExternalAccount(publicKey: PublicKey, packageName: String) {
        val externalPubkey = publicKey.toHex()
        val account = AccountEntity(pubkey = externalPubkey, packageName = packageName)
        updateAndReset(account = account)
    }

    private suspend fun updateAndReset(account: AccountEntity) {
        Log.i(tag, "Update account and reset caches")
        eventCacheClearer.clear()
        accountDao.updateAccount(account = account)
        resetDao.resetAfterAccountChange()
    }
}