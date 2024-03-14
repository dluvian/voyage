package com.dluvian.voyage.data.account

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.ResetDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import rust.nostr.protocol.PublicKey

class AccountSwitcher(
    private val mnemonicSigner: MnemonicSigner,
    private val accountDao: AccountDao,
    private val resetDao: ResetDao
) {

    suspend fun useDefaultAccount(): PubkeyHex {
        val defaultPubkey = mnemonicSigner.getPubkeyHex()
        accountDao.updateAccount(account = AccountEntity(pubkey = defaultPubkey))
        resetDao.resetAfterAccountChange()
        return defaultPubkey
    }

    suspend fun useExternalAccount(publicKey: PublicKey, packageName: String) {
        val externalPubkey = publicKey.toHex()
        val account = AccountEntity(pubkey = externalPubkey, packageName = packageName)
        accountDao.updateAccount(account = account)
        resetDao.resetAfterAccountChange()
    }
}