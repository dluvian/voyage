package com.dluvian.voyage.data.signer

import android.content.Context
import androidx.compose.runtime.State
import com.dluvian.voyage.core.model.AccountType
import rust.nostr.protocol.PublicKey

interface IAccountSwitcher {
    val accountType: State<AccountType>

    fun useDefaultAccount()
    fun useExternalAccount(publicKey: PublicKey, packageName: String)
    fun isExternalSignerInstalled(context: Context): Boolean
}
