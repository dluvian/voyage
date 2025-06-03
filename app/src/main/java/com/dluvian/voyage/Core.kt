package com.dluvian.voyage

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.model.AddPubkeyToList
import com.dluvian.voyage.model.AddTopicToList
import com.dluvian.voyage.model.BookmarkPost
import com.dluvian.voyage.model.BookmarksViewCmd
import com.dluvian.voyage.model.ClickClickableText
import com.dluvian.voyage.model.ClickNeutralizeVotes
import com.dluvian.voyage.model.ClickUpvote
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.CreateCrossPostViewCmd
import com.dluvian.voyage.model.CreateGitIssueViewCmd
import com.dluvian.voyage.model.CreatePostViewCmd
import com.dluvian.voyage.model.CreateReplyViewCmd
import com.dluvian.voyage.model.DeleteList
import com.dluvian.voyage.model.DeletePost
import com.dluvian.voyage.model.DiscoverViewCmd
import com.dluvian.voyage.model.DrawerViewCmd
import com.dluvian.voyage.model.EditListViewCmd
import com.dluvian.voyage.model.EditProfileViewCmd
import com.dluvian.voyage.model.FollowListsViewCmd
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowTopic
import com.dluvian.voyage.model.HomeViewCmd
import com.dluvian.voyage.model.InboxViewCmd
import com.dluvian.voyage.model.ListViewCmd
import com.dluvian.voyage.model.NavCmd
import com.dluvian.voyage.model.ProfileViewCmd
import com.dluvian.voyage.model.Rebroadcast
import com.dluvian.voyage.model.ReceiveEvent
import com.dluvian.voyage.model.RegisterUriHandler
import com.dluvian.voyage.model.RelayClosed
import com.dluvian.voyage.model.RelayEditorViewCmd
import com.dluvian.voyage.model.RelayNotice
import com.dluvian.voyage.model.RelayNotificationCmd
import com.dluvian.voyage.model.SearchViewCmd
import com.dluvian.voyage.model.SettingsViewCmd
import com.dluvian.voyage.model.SuggestionCmd
import com.dluvian.voyage.model.ThreadViewCmd
import com.dluvian.voyage.model.TopicViewCmd
import com.dluvian.voyage.model.UnbookmarkPost
import com.dluvian.voyage.model.UnfollowProfile
import com.dluvian.voyage.model.UnfollowTopic
import com.dluvian.voyage.navigator.Navigator
import com.dluvian.voyage.viewModel.VMContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Core(
    val vmContainer: VMContainer,
    val appContainer: AppContainer,
    val closeApp: () -> Unit,
) : ViewModel() {
    private val logTag = "Core"
    val navigator = Navigator(vmContainer = vmContainer, closeApp = closeApp)

    val onUpdate: (Cmd) -> Unit = { cmd -> handleCmd(cmd) }

    init {
        startRelayListener(appContainer.relayChannel)
        viewModelScope.launch {
            appContainer.service.init()
        }
    }

    private fun handleCmd(cmd: Cmd) {
        when (cmd) {
            is NavCmd -> navigator.handle(cmd = cmd)

            is RelayNotificationCmd -> when(cmd){
                is ReceiveEvent -> TODO()
                is RelayClosed -> TODO()
                is RelayNotice -> TODO()
            }

            is DrawerViewCmd -> vmContainer.drawerVM.handle(action = cmd)

            is ClickUpvote -> viewModelScope.launch {
                val result = appContainer.eventCreator.publishVote(cmd.event)
                if (result.isFailure){
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_upvote))
                }
                // TODO: Show relay success when user enables it in settings
            }

            is ClickNeutralizeVotes -> viewModelScope.launch {
                val ids = appContainer.upvoteProvider.upvotes(cmd.event.id())
                appContainer.eventCreator.publishDelete(eventIds = ids.toList())
                // TODO: Show error in snackbar
            }

            is FollowProfile -> viewModelScope.launch {
                appContainer.eventCreator.followProfile(cmd.pubkey)
                // TODO: Show error in snackbar
            }

            is UnfollowProfile -> viewModelScope.launch {
                appContainer.eventCreator.unfollowProfile(cmd.pubkey)
                // TODO: Show error in snackbar
            }

            is FollowTopic -> viewModelScope.launch {
                appContainer.eventCreator.followTopic(cmd.topic)
                // TODO: Show error in snackbar
            }

            is UnfollowTopic -> viewModelScope.launch {
                appContainer.eventCreator.unfollowTopic(cmd.topic)
                // TODO: Show error in snackbar
            }

            is DeletePost -> viewModelScope.launch {
                appContainer.eventCreator.publishDelete(eventIds = listOf(cmd.event.id()))
                // TODO: Show error in snackbar
            }

            is DeleteList -> viewModelScope.launch {
                appContainer.eventCreator.publishListDeletion(cmd.ident)
                // TODO: Show error in snackbar
                // TODO: Close drawer
            }

            is AddPubkeyToList -> viewModelScope.launch {
                appContainer.eventCreator.addPubkeyToList(cmd.pubkey, cmd.ident)
                // TODO: Show error in snackbar
            }

            is AddTopicToList -> viewModelScope.launch {
                appContainer.eventCreator.addTopicToList(cmd.topic, cmd.ident)
                // TODO: Show error in snackbar
            }

            is BookmarkPost -> viewModelScope.launch {
                appContainer.eventCreator.addBookmark(cmd.event.id())
                // TODO: Show error in snackbar
            }

            is UnbookmarkPost -> viewModelScope.launch {
                appContainer.eventCreator.removeBookmark(cmd.event.id())
                // TODO: Show error in snackbar
            }

            is Rebroadcast -> viewModelScope.launch {
                appContainer.service.rebroadcast(cmd.event)
                // TODO: Show error in snackbar
            }

            is ClickClickableText -> TODO()

            is SuggestionCmd -> TODO()

            is RegisterUriHandler -> {
                TODO()
//                appContainer.annotatedStringProvider
//                    .setUriHandler(uriHandler = cmd.uriHandler)
            }

            is HomeViewCmd -> vmContainer.homeVM.handle(action = cmd)
            is DiscoverViewCmd -> vmContainer.discoverVM.handle(action = cmd)
            is ThreadViewCmd -> vmContainer.threadVM.handle(action = cmd)
            is TopicViewCmd -> vmContainer.topicVM.handle(action = cmd)
            is ProfileViewCmd -> vmContainer.profileVM.handle(action = cmd)
            is SettingsViewCmd -> vmContainer.settingsVM.handle(action = cmd)
            is CreatePostViewCmd -> vmContainer.createPostVM.handle(action = cmd)
            is CreateGitIssueViewCmd -> vmContainer.createGitIssueVM.handle(action = cmd)
            is CreateReplyViewCmd -> vmContainer.createReplyVM.handle(action = cmd)
            is CreateCrossPostViewCmd -> vmContainer.createCrossPostVM.handle(action = cmd)
            is SearchViewCmd -> vmContainer.searchVM.handle(action = cmd)
            is EditProfileViewCmd -> vmContainer.editProfileVM.handle(action = cmd)
            is RelayEditorViewCmd -> vmContainer.relayEditorVM.handle(action = cmd)
            is InboxViewCmd -> vmContainer.inboxVM.handle(action = cmd)
            is FollowListsViewCmd -> vmContainer.followListsVM.handle(action = cmd)
            is BookmarksViewCmd -> vmContainer.bookmarksVM.handle(action = cmd)
            is EditListViewCmd -> vmContainer.editListVM.handle(action = cmd)
            is ListViewCmd -> vmContainer.listVM.handle(action = cmd)
        }
    }

    private fun startRelayListener(channel: Channel<RelayNotificationCmd>){
        viewModelScope.launch(Dispatchers.IO){
            while (true){
                handleCmd(channel.receive())
            }
        }.invokeOnCompletion { ex->
            Log.w(logTag, "RelayListener terminated", ex)
            startRelayListener(channel)
        }
    }

    private suspend fun showSnackbarMsg(msg: String){
        appContainer.snackbar.showSnackbar(
            message = msg,
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
    }

    override fun onCleared() {
        viewModelScope.launch {
            appContainer.service.close()
        }
        super.onCleared()
    }
}
