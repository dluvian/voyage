package com.dluvian.voyage.core.viewModel

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.ProcessExternalAccount
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SettingsViewAction
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.nostr.NostrSubscriber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.protocol.PublicKey

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val snackbar: SnackbarHostState,
    private val nostrSubscriber: NostrSubscriber,
) : ViewModel() {
    val accountType: State<AccountType> = accountManager.accountType
    val isLoadingAccount = mutableStateOf(false)
    lateinit var externalSignerHandler: ExternalSignerHandler
    private val tag = "SettingsViewModel"

    fun handle(settingsViewAction: SettingsViewAction) {
        when (settingsViewAction) {
            is UseDefaultAccount -> useDefaultAccount()

            is RequestExternalAccount -> requestExternalAccountData(
                context = settingsViewAction.context
            )

            is ProcessExternalAccount -> processExternalAccountData(
                result = settingsViewAction.activityResult,
                context = settingsViewAction.context
            )
        }
    }

    private fun useDefaultAccount() {
        if (accountType.value is DefaultAccount || isLoadingAccount.value) return
        isLoadingAccount.value = true

        viewModelScope.launch(Dispatchers.IO) {
            accountManager.useDefaultAccount()
        }.invokeOnCompletion {
            isLoadingAccount.value = false
        }
    }

    private fun requestExternalAccountData(context: Context) {
        if (accountType.value is ExternalAccount || isLoadingAccount.value) return
        isLoadingAccount.value = true

        if (!accountManager.isExternalSignerInstalled(context = context)) {
            snackbar.showToast(
                scope = viewModelScope,
                msg = context.getString(R.string.no_external_signer_installed)
            )
            isLoadingAccount.value = false
            return
        }

        val result = externalSignerHandler.requestExternalAccount()
        if (result != null) {
            isLoadingAccount.value = false
            snackbar.showToast(
                scope = viewModelScope,
                msg = context.getString(R.string.failed_to_get_permission)
            )
            Log.w(tag, "Failed to request external account", result)
        }
        // Wait for processExternalAccountData
    }

    private fun processExternalAccountData(result: ActivityResult, context: Context) {
        val npub = result.data?.getStringExtra("signature")
        val packageName = result.data?.getStringExtra("package")
        val publicKey = runCatching { PublicKey.fromBech32(npub.orEmpty()) }.getOrNull()

        if (npub == null || publicKey == null || packageName == null) {
            snackbar.showToast(
                scope = viewModelScope,
                msg = context.getString(R.string.received_invalid_data),
            )
            isLoadingAccount.value = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            accountManager.useExternalAccount(publicKey = publicKey, packageName = packageName)
        }.invokeOnCompletion {
            nostrSubscriber.subMyAccount()
            isLoadingAccount.value = false
        }
    }
}
