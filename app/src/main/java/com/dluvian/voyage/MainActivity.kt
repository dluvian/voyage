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
import com.anggrayudi.storage.SimpleStorageHelper
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.viewModel.BookmarksViewModel
import com.dluvian.voyage.core.viewModel.CreateCrossPostViewModel
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.core.viewModel.CreateReplyViewModel
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.core.viewModel.DrawerViewModel
import com.dluvian.voyage.core.viewModel.EditListViewModel
import com.dluvian.voyage.core.viewModel.EditProfileViewModel
import com.dluvian.voyage.core.viewModel.FollowListsViewModel
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.InboxViewModel
import com.dluvian.voyage.core.viewModel.ListViewModel
import com.dluvian.voyage.core.viewModel.MuteListViewModel
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
        val storageHelper = SimpleStorageHelper(this@MainActivity)

        appContainer = AppContainer(
            context = this.applicationContext,
            storageHelper = storageHelper
        )

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
    val mutedProfileListState = rememberLazyListState()
    val mutedTopicListState = rememberLazyListState()
    val mutedWordListState = rememberLazyListState()
    val bookmarksFeedState = rememberLazyListState()
    val relayEditorState = rememberLazyListState()
    val listFeedState = rememberLazyListState()
    val listProfileState = rememberLazyListState()
    val listTopicState = rememberLazyListState()

    val profilePagerState = rememberPagerState { 4 }
    val followListsPagerState = rememberPagerState { 2 }
    val muteListPagerState = rememberPagerState { 3 }
    val listViewPagerState = rememberPagerState { 4 }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    return VMContainer(
        homeVM = viewModel {
            HomeViewModel(
                muteProvider = appContainer.muteProvider,
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
                accountSwitcher = appContainer.accountSwitcher,
                snackbar = appContainer.snackbar,
                databasePreferences = appContainer.databasePreferences,
                relayPreferences = appContainer.relayPreferences,
                eventPreferences = appContainer.eventPreferences,
                databaseInteractor = appContainer.databaseInteractor,
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
                muteProvider = appContainer.muteProvider,
                postDetails = appContainer.postDetailInspector.currentDetails,
                rootFeedState = profileRootFeedState,
                replyFeedState = profileReplyFeedState,
                profileAboutState = profileAboutState,
                profileRelayState = profileRelayState,
                pagerState = profilePagerState,
                nostrSubscriber = appContainer.nostrSubscriber,
                profileProvider = appContainer.profileProvider,
                nip65Dao = appContainer.roomDb.nip65Dao(),
                eventRelayDao = appContainer.roomDb.eventRelayDao(),
                itemSetProvider = appContainer.itemSetProvider,
                myPubkeyProvider = appContainer.accountManager
            )
        },
        threadVM = viewModel {
            ThreadViewModel(
                threadProvider = appContainer.threadProvider,
                threadCollapser = appContainer.threadCollapser,
                postDetails = appContainer.postDetailInspector.currentDetails,
                threadState = threadState
            )
        },
        topicVM = viewModel {
            TopicViewModel(
                feedProvider = appContainer.feedProvider,
                muteProvider = appContainer.muteProvider,
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
                topicProvider = appContainer.topicProvider,
            )
        },
        createReplyVM = viewModel {
            CreateReplyViewModel(
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                postSender = appContainer.postSender,
                snackbar = appContainer.snackbar,
                eventRelayDao = appContainer.roomDb.eventRelayDao(),
                postDao = appContainer.roomDb.postDao(),
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
                muteProvider = appContainer.muteProvider,
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
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber
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
                muteProvider = appContainer.muteProvider,
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
                muteProvider = appContainer.muteProvider,
                postDetails = appContainer.postDetailInspector.currentDetails,
                feedState = listFeedState,
                profileState = listProfileState,
                topicState = listTopicState,
                itemSetProvider = appContainer.itemSetProvider,
                pagerState = listViewPagerState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber
            )
        },
        muteListVM = viewModel {
            MuteListViewModel(
                mutedProfileState = mutedProfileListState,
                mutedTopicState = mutedTopicListState,
                mutedWordState = mutedWordListState,
                pagerState = muteListPagerState,
                lazyNostrSubscriber = appContainer.lazyNostrSubscriber,
                profileProvider = appContainer.profileProvider,
                topicProvider = appContainer.topicProvider,
                muteProvider = appContainer.muteProvider
            )
        },
    )
}
