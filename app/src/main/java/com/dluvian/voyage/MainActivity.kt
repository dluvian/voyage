package com.dluvian.voyage

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.ProcessExternalAccount
import com.dluvian.voyage.core.ProcessExternalSignature
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.ui.VoyageApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppContainer(this.applicationContext)
        setContent {
            val activity = (LocalContext.current as? Activity)
            val closeApp: Fn = { activity?.finish() }
            val vmContainer = createVMContainer(appContainer = appContainer)
            val core = viewModel {
                Core(
                    vmContainer = vmContainer,
                    appContainer = appContainer,
                    closeApp = closeApp
                )
            }
            val externalSignerHandler = ExternalSignerHandler(
                requestAccountLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { activityResult ->
                    core.onUpdate(
                        ProcessExternalAccount(
                            activityResult = activityResult,
                            context = this.applicationContext
                        )
                    )
                },
                requestSignatureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { activityResult ->
                    core.onUpdate(ProcessExternalSignature(activityResult = activityResult))
                },
            )
            appContainer.externalSigner.externalSignerHandler = externalSignerHandler
            vmContainer.settingsVM.externalSignerHandler = externalSignerHandler
            core.externalSignerHandler = externalSignerHandler

            VoyageApp(core)
        }
    }
}

@Composable
private fun createVMContainer(appContainer: AppContainer): VMContainer {
    return VMContainer(
        homeVM = viewModel {
            HomeViewModel(
                feedProvider = appContainer.feedProvider,
                nostrSubscriber = appContainer.nostrSubscriber
            )
        },
        settingsVM = viewModel {
            SettingsViewModel(
                accountManager = appContainer.accountManager,
                snackbar = appContainer.snackbarHostState,
            )
        },
        searchVM = viewModel {
            SearchViewModel(
                topicProvider = appContainer.topicProvider
            )
        }
    )
}
