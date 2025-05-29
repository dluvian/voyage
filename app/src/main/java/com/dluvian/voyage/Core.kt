package com.dluvian.voyage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.navigator.Navigator
import com.dluvian.voyage.viewModel.VMContainer
import kotlinx.coroutines.launch

class Core(
    val vmContainer: VMContainer,
    val appContainer: AppContainer,
    val closeApp: () -> Unit,
) : ViewModel() {
    val navigator = Navigator(vmContainer = vmContainer, closeApp = closeApp)

    val onUpdate: (Cmd) -> Unit = { cmd -> handleCmd(cmd) }

    private fun handleCmd(cmd: Cmd) {
        when (cmd) {
            is NavCmd -> navigator.handle(action = cmd)

            is DrawerViewCmd -> vmContainer.drawerVM.handle(action = cmd)

            is ClickUpvote -> viewModelScope.launch {
                appContainer.nostrService.publishVote(cmd.event)
                // TODO: Show error in snackbar
            }

            is ClickNeutralizeVotes -> viewModelScope.launch {
                appContainer.nostrService.publishDelete(eventIds = cmd.voteIds)
                // TODO: Show error in snackbar
            }

            is FollowProfile -> viewModelScope.launch {
                appContainer.nostrService.followProfile(cmd.pubkey)
                // TODO: Show error in snackbar
            }

            is UnfollowProfile -> viewModelScope.launch {
                appContainer.nostrService.unfollowProfile(cmd.pubkey)
                // TODO: Show error in snackbar
            }

            is FollowTopic -> viewModelScope.launch {
                appContainer.nostrService.followTopic(cmd.topic)
                // TODO: Show error in snackbar
            }

            is UnfollowTopic -> viewModelScope.launch {
                appContainer.nostrService.unfollowTopic(cmd.topic)
                // TODO: Show error in snackbar
            }

            is DeletePost -> viewModelScope.launch {
                appContainer.nostrService.publishDelete(eventIds = listOf(cmd.event.id()))
                // TODO: Show error in snackbar
            }

            is DeleteList -> viewModelScope.launch {
                appContainer.nostrService.publishListDeletion(cmd.ident)
                // TODO: Show error in snackbar
                // TODO: Close drawer
            }

            is AddPubkeyToList -> viewModelScope.launch {
                appContainer.nostrService.addPubkeyToList(cmd.pubkey, cmd.ident)
                // TODO: Show error in snackbar
            }

            is AddTopicToList -> viewModelScope.launch {
                appContainer.nostrService.addTopicToList(cmd.topic, cmd.ident)
                // TODO: Show error in snackbar
            }

            is BookmarkPost -> viewModelScope.launch {
                appContainer.nostrService.addBookmark(cmd.event.id())
                // TODO: Show error in snackbar
            }

            is UnbookmarkPost -> viewModelScope.launch {
                appContainer.nostrService.removeBookmark(cmd.event.id())
                // TODO: Show error in snackbar
            }

            is Rebroadcast -> viewModelScope.launch {
                appContainer.nostrService.rebroadcast(cmd.event)
                // TODO: Show error in snackbar
            }

            is ClickClickableText -> TODO()

            is SuggestionCmd -> TODO()

            is RegisterUriHandler -> appContainer.annotatedStringProvider
                .setUriHandler(uriHandler = cmd.uriHandler)

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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            appContainer.nostrService.close()
        }
    }
}
