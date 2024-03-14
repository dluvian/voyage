package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.room.dao.AccountDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AccountPubkeyProvider(accountDao: AccountDao) : IPubkeyProvider {
    private val scope = CoroutineScope(Dispatchers.IO)
    val myPubkeyFlow = accountDao.getMyPubkeyFlow().stateIn(scope, SharingStarted.Eagerly, null)

    override fun tryGetPubkeyHex(): Result<PubkeyHex> {
        val pubkey = myPubkeyFlow.value
        return if (pubkey == null) Result.failure(IllegalStateException("Flow is not active yet"))
        else Result.success(pubkey)
    }


}