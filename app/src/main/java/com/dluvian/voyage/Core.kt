package com.dluvian.voyage

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.model.ItemSetProfile
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.core.navigator.Navigator
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.viewModel.VMContainer
import kotlinx.coroutines.launch

class Core(
    val vmContainer: VMContainer,
    val appContainer: AppContainer,
    val closeApp: () -> Unit,
) : ViewModel() {
    val navigator = Navigator(vmContainer = vmContainer, closeApp = closeApp)

    val onUpdate: (Cmd) -> Unit = { cmd ->
        when (cmd) {
            is NavCmd -> navigator.handle(action = cmd)

            is DrawerViewAction -> vmContainer.drawerVM.handle(action = cmd)

            is VoteCmd -> when (cmd) {
                is ClickUpvote -> viewModelScope.launch {
                    appContainer.nostrService.publishVote(cmd.event)
                    // TODO: Show error in snackbar
                }

                is ClickNeutralizeVotes -> viewModelScope.launch {
                    appContainer.nostrService.publishDelete(eventIds = cmd.voteIds)
                    // TODO: Show error in snackbar
                }
            }

            is ProfileCmd -> when (cmd) {
                is FollowProfile -> viewModelScope.launch {
                    appContainer.nostrService.followProfile(cmd.pubkey)
                    // TODO: Show error in snackbar
                }

                is UnfollowProfile -> viewModelScope.launch {
                    appContainer.nostrService.unfollowProfile(cmd.pubkey)
                    // TODO: Show error in snackbar
                }
            }

            is TopicCmd -> when (cmd) {
                is FollowTopic -> viewModelScope.launch {
                    appContainer.nostrService.followTopic(cmd.topic)
                    // TODO: Show error in snackbar
                }

                is UnfollowTopic -> viewModelScope.launch {
                    appContainer.nostrService.unfollowTopic(cmd.topic)
                    // TODO: Show error in snackbar
                }
            }
            is DeletePost -> viewModelScope.launchIO {
                appContainer.eventDeletor.deletePost(postId = cmd.id)
            }

            is DeleteList -> viewModelScope.launchIO {
                appContainer.eventDeletor.deleteList(
                    identifier = cmd.identifier,
                    onCloseDrawer = cmd.onCloseDrawer
                )
            }

            is AddItemToList -> viewModelScope.launchIO {
                appContainer.itemSetEditor.addItemToSet(
                    item = cmd.item,
                    identifier = cmd.identifier
                ).onFailure {
                    val errId = when (cmd.item) {
                        is ItemSetProfile -> R.string.failed_to_sign_profile_list
                        is ItemSetTopic -> R.string.failed_to_sign_topic_list
                    }
                    appContainer.snackbar.showToast(
                        scope = cmd.scope,
                        msg = cmd.context.getString(errId)
                    )
                }.onSuccess {
                    appContainer.snackbar.showToast(
                        scope = cmd.scope,
                        msg = cmd.context.getString(R.string.added_to_list)
                    )
                }
            }

            is BookmarkCmd -> appContainer.bookmarker.handle(action = cmd)
            is HomeViewAction -> vmContainer.homeVM.handle(action = cmd)
            is DiscoverViewAction -> vmContainer.discoverVM.handle(action = cmd)
            is ThreadViewAction -> vmContainer.threadVM.handle(action = cmd)
            is TopicViewAction -> vmContainer.topicVM.handle(action = cmd)
            is ProfileViewAction -> vmContainer.profileVM.handle(action = cmd)
            is SettingsViewAction -> vmContainer.settingsVM.handle(action = cmd)
            is CreatePostViewAction -> vmContainer.createPostVM.handle(action = cmd)
            is CreateGitIssueViewAction -> vmContainer.createGitIssueVM.handle(action = cmd)
            is CreateReplyViewAction -> vmContainer.createReplyVM.handle(action = cmd)
            is CreateCrossPostViewAction -> vmContainer.createCrossPostVM.handle(action = cmd)
            is SearchViewAction -> vmContainer.searchVM.handle(action = cmd)
            is EditProfileViewAction -> vmContainer.editProfileVM.handle(action = cmd)
            is RelayEditorViewAction -> vmContainer.relayEditorVM.handle(action = cmd)
            is InboxViewAction -> vmContainer.inboxVM.handle(action = cmd)
            is FollowListsViewAction -> vmContainer.followListsVM.handle(action = cmd)
            is BookmarksViewAction -> vmContainer.bookmarksVM.handle(action = cmd)
            is EditListViewAction -> vmContainer.editListVM.handle(action = cmd)
            is ListViewAction -> vmContainer.listVM.handle(action = cmd)

            is ProcessExternalSignature -> viewModelScope.launch {
                appContainer.externalSignerHandler.processExternalSignature(
                    result = cmd.activityResult
                )
            }

            is ClickClickableText -> clickText(action = cmd)

            is SuggestionAction -> appContainer.suggestionProvider.handle(action = cmd)

            is RegisterAccountLauncher -> appContainer.externalSignerHandler
                .setAccountLauncher(launcher = cmd.launcher)

            is RegisterSignerLauncher -> appContainer.externalSignerHandler
                .setSignerLauncher(launcher = cmd.launcher)

            is RegisterUriHandler -> appContainer.annotatedStringProvider
                .setUriHandler(uriHandler = cmd.uriHandler)


            is Rebroadcast -> viewModelScope.launchIO {
                appContainer.eventRebroadcaster.rebroadcast(
                    postId = cmd.postId,
                    context = cmd.context,
                    uiScope = viewModelScope
                )
            }

            ClosePostInfo -> appContainer.postDetailInspector.closePostDetails()
            is OpenPostInfo -> viewModelScope.launchIO {
                appContainer.postDetailInspector.setPostDetails(postId = cmd.postId)
            }

            is OpenLightningWallet -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("lightning:${cmd.address}")
                )
                runCatching { cmd.launcher.launch(intent) }
                    .onFailure {
                        appContainer.snackbar.showToast(
                            scope = cmd.scope,
                            msg = appContainer.context.getString(R.string.you_dont_have_a_lightning_wallet_installed)
                        )
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        appContainer.nostrService.close()
    }
}
