package com.dluvian.voyage.data.signer

import android.content.Context
import androidx.compose.runtime.State
import com.dluvian.voyage.core.model.AccountType

interface IAccountSwitcher {
    val accountType: State<AccountType>

    fun useDefaultAccount()
    fun useExternalAccount(): Boolean
    fun isExternalSignerInstalled(context: Context): Boolean
}
