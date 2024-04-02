package com.dluvian.voyage.core.viewModel

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.ProcessExternalAccount
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SettingsViewAction
import com.dluvian.voyage.core.UpdateRootPostThreshold
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.account.AccountSwitcher
import com.dluvian.voyage.data.preferences.DatabasePreferences
import com.dluvian.voyage.data.provider.DatabaseStatProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.PublicKey

class SettingsViewModel(
    private val accountSwitcher: AccountSwitcher,
    private val snackbar: SnackbarHostState,
    private val databasePreferences: DatabasePreferences,
    databaseStatProvider: DatabaseStatProvider,
) : ViewModel() {
    val accountType: State<AccountType> = accountSwitcher.accountType
    val rootPostThreshold = mutableIntStateOf(databasePreferences.getSweepThreshold())
    val currentRootPostCount = databaseStatProvider.getRootPostCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val isLoadingAccount = mutableStateOf(false)
    lateinit var externalSignerHandler: ExternalSignerHandler
    private val tag = "SettingsViewModel"

    fun handle(action: SettingsViewAction) {
        when (action) {
            is UseDefaultAccount -> useDefaultAccount()

            is RequestExternalAccount -> requestExternalAccountData(context = action.context)

            is ProcessExternalAccount -> processExternalAccountData(
                result = action.activityResult,
                context = action.context
            )

            is UpdateRootPostThreshold -> {
                val newThreshold = action.threshold.toInt()
                rootPostThreshold.intValue = newThreshold
                databasePreferences.setSweepThreshold(newThreshold = newThreshold)
            }
        }
    }

    private fun useDefaultAccount() {
        if (accountType.value is DefaultAccount || isLoadingAccount.value) return
        isLoadingAccount.value = true

        viewModelScope.launchIO {
            accountSwitcher.useDefaultAccount()
        }.invokeOnCompletion {
            isLoadingAccount.value = false
        }
    }

    private fun requestExternalAccountData(context: Context) {
        if (accountType.value is ExternalAccount || isLoadingAccount.value) return
        isLoadingAccount.value = true

        if (!accountSwitcher.isExternalSignerInstalled(context = context)) {
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

        viewModelScope.launchIO {
            accountSwitcher.useExternalAccount(publicKey = publicKey, packageName = packageName)
        }.invokeOnCompletion {
            isLoadingAccount.value = false
        }
    }
}
