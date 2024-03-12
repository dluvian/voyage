package com.dluvian.voyage.core.viewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.ProcessExternalAccountData
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SettingsViewAction
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.data.signer.IAccountSwitcher
import rust.nostr.protocol.PublicKey

class SettingsViewModel(
    private val accountSwitcher: IAccountSwitcher
) : ViewModel() {
    val accountType: State<AccountType> = accountSwitcher.accountType

    fun handle(settingsViewAction: SettingsViewAction) {
        when (settingsViewAction) {
            is UseDefaultAccount -> accountSwitcher.useDefaultAccount()
            is RequestExternalAccount -> requestExternalAccountData(
                context = settingsViewAction.context,
                launcher = settingsViewAction.launcher
            )

            is ProcessExternalAccountData -> processExternalAccountData(result = settingsViewAction.activityResult)
        }
    }

    private fun requestExternalAccountData(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        if (accountType.value is ExternalAccount) return

        if (accountSwitcher.isExternalSignerInstalled(context = context)) {
            // TODO: Toast
            Log.w("LOLOL", "detected")
        } else {
            Log.w("LOLOL", "not detected")
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))
        intent.putExtra("permissions", "[{\"type\":\"get_public_key\"}]")
        intent.putExtra("type", "get_public_key")
        launcher.launch(intent)
    }

    private fun processExternalAccountData(result: ActivityResult) {
        // TODO: Toast when null
        val npub = result.data?.getStringExtra("signature") ?: return
        val packageName = result.data?.getStringExtra("package") ?: return
        val publicKey = runCatching { PublicKey.fromBech32(npub) }.getOrNull() ?: return

        accountSwitcher.useExternalAccount(publicKey = publicKey, packageName = packageName)
    }
}
