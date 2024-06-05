package com.dluvian.voyage

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.viewModel.BookmarksViewModel
import com.dluvian.voyage.core.viewModel.CreateCrossPostViewModel
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.core.viewModel.CreateReplyViewModel
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.core.viewModel.DrawerViewModel
import com.dluvian.voyage.core.viewModel.EditProfileViewModel
import com.dluvian.voyage.core.viewModel.FollowListsViewModel
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.InboxViewModel
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.core.viewModel.RelayEditorViewModel
import com.dluvian.voyage.core.viewModel.RelayProfileViewModel
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.core.viewModel.TopicViewModel
import com.dluvian.voyage.ui.VoyageApp

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(context = this.applicationContext)
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
            core.handleDeeplink(intent = intent)

            VoyageApp(core)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")

        appContainer.eventSweeper.sweep()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun createVMContainer(appContainer: AppContainer): VMContainer {
    // We define states in upper level so that it keeps the scroll position when popping the nav stack
    val homeFeedState = rememberLazyListState()
    val profileRootFeedState = rememberLazyListState()
    val profileReplyFeedState = rememberLazyListState()
    val profileAboutState = rememberLazyListState()
    val profileRelayState = rememberLazyListState()
    val topicFeedState = rememberLazyListState()
    val threadState = rememberLazyListState()
    val inboxFeedState = rememberLazyListState()
    val contactListState = rememberLazyListState()
    val topicListState = rememberLazyListState()
    val bookmarksFeedState = rememberLazyListState()
    val relayEditorState = rememberLazyListState()

    val profilePagerState = rememberPagerState { 4 }
    val followListsPagerState = rememberPagerState { 2 }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    return VMContainer(
        homeVM = viewModel {
            HomeViewModel(
                feedProvider = appContainer.feedProvider,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                feedState = homeFeedState,
            )
        },
        discoverVM = viewModel {
            DiscoverViewModel(
                topicProvider = appContainer.topicProvider,
                profileProvider = appContainer.profileProvider,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber
            )
        },
        settingsVM = viewModel {
            SettingsViewModel(
                accountSwitcher = appContainer.accountSwitcher,
                snackbar = appContainer.snackbar,
                databasePreferences = appContainer.databasePreferences,
                relayPreferences = appContainer.relayPreferences,
                databaseStatProvider = appContainer.databaseStatProvider,
                externalSignerHandler = appContainer.externalSignerHandler,
                mnemonicSigner = appContainer.mnemonicSigner,
            )
        },
        searchVM = viewModel {
            SearchViewModel(
                searchProvider = appContainer.searchProvider,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                snackbar = appContainer.snackbar,
            )
        },
        profileVM = viewModel {
            ProfileViewModel(
                feedProvider = appContainer.feedProvider,
                subCreator = appContainer.nostrSubscriber.subCreator,
                profileProvider = appContainer.profileProvider,
                nip65Dao = appContainer.roomDb.nip65Dao(),
                eventRelayDao = appContainer.roomDb.eventRelayDao(),
                rootFeedState = profileRootFeedState,
                replyFeedState = profileReplyFeedState,
                profileAboutState = profileAboutState,
                profileRelayState = profileRelayState,
                pagerState = profilePagerState,
            )
        },
        threadVM = viewModel {
            ThreadViewModel(
                threadProvider = appContainer.threadProvider,
                threadCollapser = appContainer.threadCollapser,
                threadState = threadState
            )
        },
        topicVM = viewModel {
            TopicViewModel(
                subCreator = appContainer.subCreator,
                feedProvider = appContainer.feedProvider,
                topicProvider = appContainer.topicProvider,
                feedState = topicFeedState,
            )
        },
        createPostVM = viewModel {
            CreatePostViewModel(
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar,
                topicProvider = appContainer.topicProvider,
            )
        },
        createReplyVM = viewModel {
            CreateReplyViewModel(
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar,
                eventRelayDao = appContainer.roomDb.eventRelayDao()
            )
        },
        editProfileVM = viewModel {
            EditProfileViewModel(
                fullProfileUpsertDao = appContainer.roomDb.fullProfileUpsertDao(),
                nostrService = appContainer.nostrService,
                snackbar = appContainer.snackbar,
                relayProvider = appContainer.relayProvider,
                fullProfileDao = appContainer.roomDb.fullProfileDao(),
                metadataInMemory = appContainer.metadataInMemory,
                profileUpsertDao = appContainer.roomDb.profileUpsertDao()
            )
        },
        relayEditorVM = viewModel {
            RelayEditorViewModel(
                lazyListState = relayEditorState,
                relayProvider = appContainer.relayProvider,
                snackbar = appContainer.snackbar,
                nostrService = appContainer.nostrService,
                nip65UpsertDao = appContainer.roomDb.nip65UpsertDao(),
                connectionStatuses = appContainer.connectionStatuses
            )
        },
        createCrossPostVM = viewModel {
            CreateCrossPostViewModel(
                topicProvider = appContainer.topicProvider,
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar
            )
        },
        relayProfileVM = viewModel {
            RelayProfileViewModel(
                relayProfileProvider = appContainer.relayProfileProvider,
                countDao = appContainer.roomDb.countDao(),
            )
        },
        inboxVM = viewModel {
            InboxViewModel(
                feedProvider = appContainer.feedProvider,
                subCreator = appContainer.lazyNostrSubscriber.subCreator,
                feedState = inboxFeedState,
            )
        },
        drawerVM = viewModel {
            DrawerViewModel(
                profileProvider = appContainer.profileProvider,
                drawerState = drawerState,
            )
        },
        followListsVM = viewModel {
            FollowListsViewModel(
                contactListState = contactListState,
                topicListState = topicListState,
                pagerState = followListsPagerState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                profileProvider = appContainer.profileProvider,
                topicProvider = appContainer.topicProvider
            )
        },
        bookmarksVM = viewModel {
            BookmarksViewModel(
                feedProvider = appContainer.feedProvider,
                feedState = bookmarksFeedState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
            )
        }
    )
}
