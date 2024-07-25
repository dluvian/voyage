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
import com.dluvian.voyage.core.ExportDatabase
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.LoadSeed
import com.dluvian.voyage.core.ProcessExternalAccount
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SendAuth
import com.dluvian.voyage.core.SettingsViewAction
import com.dluvian.voyage.core.UpdateLocalRelayPort
import com.dluvian.voyage.core.UpdateRootPostThreshold
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.account.AccountSwitcher
import com.dluvian.voyage.data.account.MnemonicSigner
import com.dluvian.voyage.data.preferences.DatabasePreferences
import com.dluvian.voyage.data.preferences.RelayPreferences
import com.dluvian.voyage.data.provider.DatabaseInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.PublicKey

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val accountSwitcher: AccountSwitcher,
    private val snackbar: SnackbarHostState,
    private val databasePreferences: DatabasePreferences,
    private val relayPreferences: RelayPreferences,
    private val databaseInteractor: DatabaseInteractor,
    private val externalSignerHandler: ExternalSignerHandler,
    private val mnemonicSigner: MnemonicSigner,
) : ViewModel() {
    val accountType: State<AccountType> = accountSwitcher.accountType
    val rootPostThreshold = mutableIntStateOf(databasePreferences.getSweepThreshold())
    val currentRootPostCount = databaseInteractor.getRootPostCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val seed = mutableStateOf(emptyList<String>())
    val sendAuth = mutableStateOf(relayPreferences.getSendAuth())
    val localRelayPort = mutableStateOf(relayPreferences.getLocalRelayPort())
    val isExporting = mutableStateOf(false)
    val exportCount = mutableIntStateOf(0)

    val isLoadingAccount = mutableStateOf(false)

    fun handle(action: SettingsViewAction) {
        when (action) {
            is UseDefaultAccount -> useDefaultAccount()

            is RequestExternalAccount -> requestExternalAccountData(action = action)

            is ProcessExternalAccount -> processExternalAccountData(
                result = action.activityResult,
                context = action.context
            )

            is UpdateRootPostThreshold -> {
                val newThreshold = action.threshold.toInt()
                rootPostThreshold.intValue = newThreshold
                databasePreferences.setSweepThreshold(newThreshold = newThreshold)
            }

            is LoadSeed -> seed.value = mnemonicSigner.getSeed()

            is SendAuth -> {
                relayPreferences.setSendAuth(sendAuth = action.sendAuth)
                this.sendAuth.value = action.sendAuth
            }

            is UpdateLocalRelayPort -> {
                relayPreferences.setLocalRelayPort(port = action.port)
                this.localRelayPort.value = action.port
            }

            is ExportDatabase -> exportDatabase(uiScope = action.uiScope)
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

    private fun requestExternalAccountData(action: RequestExternalAccount) {
        if (accountType.value is ExternalAccount || isLoadingAccount.value) return
        isLoadingAccount.value = true

        if (!accountSwitcher.isExternalSignerInstalled(context = action.context)) {
            snackbar.showToast(
                scope = viewModelScope,
                msg = action.context.getString(R.string.no_external_signer_installed)
            )
            isLoadingAccount.value = false
            return
        }

        val result = externalSignerHandler.requestExternalAccount()
        if (result != null) {
            isLoadingAccount.value = false
            snackbar.showToast(
                scope = viewModelScope,
                msg = action.context.getString(R.string.failed_to_get_permission)
            )
            Log.w(TAG, "Failed to request external account", result)
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

    private fun exportDatabase(uiScope: CoroutineScope) {
        if (isExporting.value) return
        isExporting.value = true

        viewModelScope.launchIO {
            databaseInteractor.exportMyPostsAndBookmarks(
                uiScope = uiScope,
                onSetExportCount = { count -> exportCount.intValue = count },
                onFinishExport = {
                    exportCount.intValue = 0
                    isExporting.value = false
                }
            )
        }
    }
}
