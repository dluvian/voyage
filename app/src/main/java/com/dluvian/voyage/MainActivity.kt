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
import com.dluvian.voyage.viewModel.BookmarksViewModel
import com.dluvian.voyage.viewModel.CreateCrossPostViewModel
import com.dluvian.voyage.viewModel.CreateGitIssueViewModel
import com.dluvian.voyage.viewModel.CreatePostViewModel
import com.dluvian.voyage.viewModel.CreateReplyViewModel
import com.dluvian.voyage.viewModel.DiscoverViewModel
import com.dluvian.voyage.viewModel.DrawerViewModel
import com.dluvian.voyage.viewModel.EditListViewModel
import com.dluvian.voyage.viewModel.EditProfileViewModel
import com.dluvian.voyage.viewModel.FollowListsViewModel
import com.dluvian.voyage.viewModel.HomeViewModel
import com.dluvian.voyage.viewModel.InboxViewModel
import com.dluvian.voyage.viewModel.ListViewModel
import com.dluvian.voyage.viewModel.ProfileViewModel
import com.dluvian.voyage.viewModel.RelayEditorViewModel
import com.dluvian.voyage.viewModel.RelayProfileViewModel
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
            appContainer.annotatedStringProvider.setOnUpdate(onUpdate = core.onUpdate)

            VoyageApp(core)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")

        CoroutineScope(Dispatchers.IO).launch {
            appContainer.nostrService.dbRemoveOldData()
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
    val listFeedState = rememberLazyListState()
    val listProfileState = rememberLazyListState()
    val listTopicState = rememberLazyListState()

    val profilePagerState = rememberPagerState { 3 }
    val followListsPagerState = rememberPagerState { 2 }
    val listViewPagerState = rememberPagerState { 4 }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    return VMContainer(
        homeVM = viewModel {
            HomeViewModel(
                feedProvider = appContainer.feedProvider,
                postDetails = appContainer.postDetailInspector.currentDetails,
                feedState = homeFeedState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                homePreferences = appContainer.homePreferences
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
                snackbar = appContainer.snackbar,
                relayPreferences = appContainer.relayPreferences,
                eventPreferences = appContainer.eventPreferences,
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
                rootFeedState = profileRootFeedState,
                replyFeedState = profileReplyFeedState,
                profileAboutState = profileAboutState,
                profileRelayState = profileRelayState,
                pagerState = profilePagerState,
                nostrSubscriber = appContainer.nostrSubscriber,
                profileProvider = appContainer.profileProvider,
                itemSetProvider = appContainer.itemSetProvider,
                myPubkeyProvider = appContainer.accountManager,
            )
        },
        threadVM = viewModel {
            ThreadViewModel(
                postDetails = appContainer.postDetailInspector.currentDetails,
                threadState = threadState,
                threadProvider = appContainer.threadProvider,
                threadCollapser = appContainer.threadCollapser,
            )
        },
        topicVM = viewModel {
            TopicViewModel(
                feedProvider = appContainer.feedProvider,
                postDetails = appContainer.postDetailInspector.currentDetails,
                feedState = topicFeedState,
                subCreator = appContainer.subCreator,
                topicProvider = appContainer.topicProvider,
                itemSetProvider = appContainer.itemSetProvider,
            )
        },
        createPostVM = viewModel {
            CreatePostViewModel(
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar,
            )
        },
        createReplyVM = viewModel {
            CreateReplyViewModel(
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar,
                eventRelayDao = appContainer.roomDb.eventRelayDao(),
                mainEventDao = appContainer.roomDb.mainEventDao(),
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
                postDetails = appContainer.postDetailInspector.currentDetails,
                feedState = inboxFeedState,
                inboxPreferences = appContainer.inboxPreferences
            )
        },
        drawerVM = viewModel {
            DrawerViewModel(
                profileProvider = appContainer.profileProvider,
                itemSetProvider = appContainer.itemSetProvider,
                drawerState = drawerState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
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
                postDetails = appContainer.postDetailInspector.currentDetails,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
            )
        },
        editListVM = viewModel {
            EditListViewModel(
                itemSetEditor = appContainer.itemSetEditor,
                snackbar = appContainer.snackbar,
                itemSetProvider = appContainer.itemSetProvider,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber
            )
        },
        listVM = viewModel {
            ListViewModel(
                feedProvider = appContainer.feedProvider,
                postDetails = appContainer.postDetailInspector.currentDetails,
                feedState = listFeedState,
                profileState = listProfileState,
                topicState = listTopicState,
                itemSetProvider = appContainer.itemSetProvider,
                pagerState = listViewPagerState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber
            )
        },
        createGitIssueVM = viewModel {
            CreateGitIssueViewModel(
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber
            )
        },
    )
}
