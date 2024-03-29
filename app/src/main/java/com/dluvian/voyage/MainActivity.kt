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
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.core.viewModel.TopicViewModel
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
        discoverVM = viewModel {
            DiscoverViewModel(
                topicProvider = appContainer.topicProvider,
                profileProvider = appContainer.profileProvider,
                topicFollower = appContainer.topicFollower,
                profileFollower = appContainer.profileFollower,
            )
        },
        settingsVM = viewModel {
            SettingsViewModel(
                accountSwitcher = appContainer.accountSwitcher,
                snackbar = appContainer.snackbar,
            )
        },
        searchVM = viewModel {
            SearchViewModel(
                suggestionProvider = appContainer.suggestionProvider,
                nostrSubscriber = appContainer.nostrSubscriber,
                snackbar = appContainer.snackbar,
            )
        },
        profileVM = viewModel {
            ProfileViewModel(
                feedProvider = appContainer.feedProvider,
                nostrSubscriber = appContainer.nostrSubscriber,
                profileFollower = appContainer.profileFollower,
                profileProvider = appContainer.profileProvider,
            )
        },
        threadVM = viewModel {
            ThreadViewModel(
                threadProvider = appContainer.threadProvider,
                threadCollapser = appContainer.threadCollapser
            )
        },
        topicVM = viewModel {
            TopicViewModel(
                feedProvider = appContainer.feedProvider,
                topicProvider = appContainer.topicProvider,
                topicFollower = appContainer.topicFollower,
            )
        },
        createPostVM = viewModel {
            CreatePostViewModel(
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar
            )
        }
    )
}
