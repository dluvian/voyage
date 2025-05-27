package com.dluvian.voyage.viewModel

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.AddClientTag
import com.dluvian.voyage.ChangeUpvoteContent
import com.dluvian.voyage.ExportDatabase
import com.dluvian.voyage.LoadSeed
import com.dluvian.voyage.ProcessExternalAccount
import com.dluvian.voyage.R
import com.dluvian.voyage.RequestExternalAccount
import com.dluvian.voyage.ResetDatabase
import com.dluvian.voyage.SendAuth
import com.dluvian.voyage.SettingsViewAction
import com.dluvian.voyage.UseDefaultAccount
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.provider.DatabaseInteractor
import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import rust.nostr.sdk.PublicKey
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val snackbar: SnackbarHostState,
    private val relayPreferences: RelayPreferences,
    private val eventPreferences: EventPreferences,
    private val databaseInteractor: DatabaseInteractor,
    private val externalSignerHandler: ExternalSignerHandler,
) : ViewModel() {
    val seed = mutableStateOf(emptyList<String>())
    val sendAuth = mutableStateOf(relayPreferences.getSendAuth())
    val isDeleting = mutableStateOf(false)
    val isExporting = mutableStateOf(false)
    val exportCount = mutableIntStateOf(0)
    val currentUpvote = mutableStateOf(eventPreferences.getUpvoteContent())
    val isAddingClientTag = mutableStateOf(eventPreferences.isAddingClientTag())

    val isLoadingAccount = mutableStateOf(false)

    fun handle(action: SettingsViewAction) {
        when (action) {
            is UseDefaultAccount -> useDefaultAccount()

            is RequestExternalAccount -> requestExternalAccountData(action = action)

            is ProcessExternalAccount -> processExternalAccountData(
                result = action.activityResult,
                context = action.context
            )

            is LoadSeed -> seed.value = mnemonicSigner.getSeed()

            is SendAuth -> {
                relayPreferences.setSendAuth(sendAuth = action.sendAuth)
                this.sendAuth.value = action.sendAuth
            }

            is ChangeUpvoteContent -> {
                eventPreferences.setUpvoteContent(newUpvote = action.newContent)
                this.currentUpvote.value = action.newContent
            }

            is AddClientTag -> {
                eventPreferences.setIsAddingClientTag(addClientTag = action.addClientTag)
                this.isAddingClientTag.value = action.addClientTag
            }

            is ExportDatabase -> exportDatabase(uiScope = action.uiScope)

            is ResetDatabase -> deleteAllPosts(uiScope = action.uiScope)
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
        val npubOrPubkey = result.data?.getStringExtra("signature")
        val packageName = result.data?.getStringExtra("package")
        val publicKey = runCatching { PublicKey.parse(npubOrPubkey.orEmpty()) }.getOrNull()

        if (npubOrPubkey == null || publicKey == null || packageName == null) {
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

    private val isExportable = AtomicBoolean(true)
    private fun exportDatabase(uiScope: CoroutineScope) {
        if (!isExportable.compareAndSet(true, false)) return

        viewModelScope.launchIO {
            databaseInteractor.exportDatabase(
                uiScope = uiScope,
                onSetExportCount = { count -> exportCount.intValue = count },
                onStartExport = { isExporting.value = true },
                onFinishExport = {
                    exportCount.intValue = 0
                    isExporting.value = false
                }
            )
        }.invokeOnCompletion { isExportable.set(true) }
    }

    private fun deleteAllPosts(uiScope: CoroutineScope) {
        if (isDeleting.value) return
        isDeleting.value = true

        viewModelScope.launchIO {
            delay(DEBOUNCE)
            databaseInteractor.clearDatabase(uiScope = uiScope)
        }.invokeOnCompletion {
            isDeleting.value = false
        }
    }
}
