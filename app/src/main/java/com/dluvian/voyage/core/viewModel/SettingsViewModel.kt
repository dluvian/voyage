package com.dluvian.voyage.core.viewModel

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.SettingsViewAction
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.UseExternalAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.data.signer.IAccountSwitcher

class SettingsViewModel(
    private val accountSwitcher: IAccountSwitcher
) : ViewModel() {
    val accountType: State<AccountType> = accountSwitcher.accountType

    fun handle(settingsViewAction: SettingsViewAction) {
        when (settingsViewAction) {
            is UseDefaultAccount -> accountSwitcher.useDefaultAccount()
            is UseExternalAccount -> requestExternalAccountInfo(launcher = settingsViewAction.launcher)
        }
    }

    fun requestExternalAccountInfo(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (accountType.value is ExternalAccount) return

//        if (accountSwitcher.isExternalSignerInstalled(context = context)){
//            Log.w("LOLOL", "detected")
//        } else{
//            Log.w("LOLOL", "not detected")
//        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))
        intent.putExtra("permissions", "[{\"type\":\"get_public_key\"}]")
        intent.putExtra("type", "get_public_key")
        launcher.launch(intent)
    }
}
