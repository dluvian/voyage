package com.dluvian.voyage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dluvian.voyage.ui.VoyageApp
import com.dluvian.voyage.viewModel.BookmarkViewModel
import com.dluvian.voyage.viewModel.CrossPostViewModel
import com.dluvian.voyage.viewModel.DiscoverViewModel
import com.dluvian.voyage.viewModel.DrawerViewModel
import com.dluvian.voyage.viewModel.EditProfileViewModel
import com.dluvian.voyage.viewModel.FollowListsViewModel
import com.dluvian.voyage.viewModel.GitIssueViewModel
import com.dluvian.voyage.viewModel.HomeViewModel
import com.dluvian.voyage.viewModel.InboxViewModel
import com.dluvian.voyage.viewModel.PostViewModel
import com.dluvian.voyage.viewModel.ProfileViewModel
import com.dluvian.voyage.viewModel.RelayEditorViewModel
import com.dluvian.voyage.viewModel.RelayProfileViewModel
import com.dluvian.voyage.viewModel.ReplyViewModel
import com.dluvian.voyage.viewModel.SearchViewModel
import com.dluvian.voyage.viewModel.SettingsViewModel
import com.dluvian.voyage.viewModel.ThreadViewModel
import com.dluvian.voyage.viewModel.TopicViewModel
import com.dluvian.voyage.viewModel.VMContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        appContainer = AppContainer(this.applicationContext)

        setContent {
            val activity = LocalActivity.current
            val closeApp: () -> Unit = { activity?.finish() }
            val vmContainer = createVMContainer(appContainer = appContainer)
            val core = viewModel {
                Core(
                    vmContainer = vmContainer,
                    appContainer = appContainer,
                    closeApp = closeApp
                )
            }

            VoyageApp(core)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")

        CoroutineScope(Dispatchers.IO).launch {
            appContainer.dbSweeper.sweep()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }
}

@Composable
private fun createVMContainer(appContainer: AppContainer): VMContainer {
    // We define states in upper level so that it keeps the scroll position when popping the nav stack
    val homeFeedState = rememberLazyListState()
    val profileFeedState = rememberLazyListState()
    val profileAboutState = rememberLazyListState()
    val profileRelayState = rememberLazyListState()
    val topicFeedState = rememberLazyListState()
    val threadState = rememberLazyListState()
    val inboxFeedState = rememberLazyListState()
    val contactListState = rememberLazyListState()
    val topicListState = rememberLazyListState()
    val bookmarksFeedState = rememberLazyListState()
    val relayEditorState = rememberLazyListState()

    val profilePagerState = rememberPagerState { 3 }
    val followListsPagerState = rememberPagerState { 2 }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    return VMContainer(
        homeVM = viewModel {
            HomeViewModel(
                feedState = homeFeedState,
                feedProvider = appContainer.feedProvider,
                homePreferences = appContainer.homePreferences,
                service = appContainer.service
            )
        },
        discoverVM = viewModel {
            DiscoverViewModel(appContainer.topicProvider, appContainer.service)
        },
        settingsVM = viewModel {
            SettingsViewModel(
                nameProvider = appContainer.nameProvider,
                service = appContainer.service,
                relayPreferences = appContainer.relayPreferences,
                eventPreferences = appContainer.eventPreferences,
            )
        },
        searchVM = viewModel {
            SearchViewModel(appContainer.service, appContainer.nameProvider)
        },
        profileVM = viewModel {
            ProfileViewModel(
                profileFeedState = profileFeedState,
                profileAboutState = profileAboutState,
                profileRelayState = profileRelayState,
                pagerState = profilePagerState,
                feedProvider = appContainer.feedProvider,
                service = appContainer.service
            )
        },
        threadVM = viewModel {
            ThreadViewModel(
                threadState = threadState,
                service = appContainer.service,
                oldestUsedTimestampProvider = appContainer.oldestUsedTimestampProvider
            )
        },
        topicVM = viewModel {
            TopicViewModel(
                feedProvider = appContainer.feedProvider,
                feedState = topicFeedState,
                topicProvider = appContainer.topicProvider,
            )
        },
        postVM = viewModel {
            PostViewModel()
        },
        replyVM = viewModel {
            ReplyViewModel()
        },
        editProfileVM = viewModel {
            EditProfileViewModel(appContainer.profileProvider)
        },
        relayEditorVM = viewModel {
            RelayEditorViewModel(relayEditorState, appContainer.service)
        },
        crossPostVM = viewModel {
            CrossPostViewModel()
        },
        relayProfileVM = viewModel {
            RelayProfileViewModel(appContainer.service)
        },
        inboxVM = viewModel {
            InboxViewModel(
                feedProvider = appContainer.feedProvider,
                feedState = inboxFeedState,
                inboxPreferences = appContainer.inboxPreferences
            )
        },
        drawerVM = viewModel {
            DrawerViewModel(
                drawerState = drawerState,
                trustProvider = appContainer.trustProvider,
                nameProvider = appContainer.nameProvider
            )
        },
        followListsVM = viewModel {
            FollowListsViewModel(
                contactListState = contactListState,
                topicListState = topicListState,
                pagerState = followListsPagerState,
                service = appContainer.service,
                nameProvider = appContainer.nameProvider
            )
        },
        bookmarkVM = viewModel {
            BookmarkViewModel(
                feedProvider = appContainer.feedProvider,
                feedState = bookmarksFeedState,
                service = appContainer.service
            )
        },
        gitIssueVM = viewModel {
            GitIssueViewModel()
        },
    )
}
