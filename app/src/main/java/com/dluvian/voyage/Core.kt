package com.dluvian.voyage

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.model.AddPubkeyToList
import com.dluvian.voyage.model.AddTopicToList
import com.dluvian.voyage.model.BookmarkPost
import com.dluvian.voyage.model.BookmarkViewCmd
import com.dluvian.voyage.model.ClickNeutralizeVotes
import com.dluvian.voyage.model.ClickUpvote
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.CreateGitIssueViewCmd
import com.dluvian.voyage.model.CreatePostViewCmd
import com.dluvian.voyage.model.CreateReplyViewCmd
import com.dluvian.voyage.model.CrossPostViewCmd
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
import com.dluvian.voyage.model.RelayClosed
import com.dluvian.voyage.model.RelayEditorViewCmd
import com.dluvian.voyage.model.RelayNotice
import com.dluvian.voyage.model.RelayNotificationCmd
import com.dluvian.voyage.model.SearchViewCmd
import com.dluvian.voyage.model.SettingsViewCmd
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
    val navigator = Navigator(vmContainer = this@Core.bookmarkVM, closeApp = closeApp)

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

            is RelayNotificationCmd -> when (cmd) {
                is ReceiveEvent -> {
                    viewModelScope.launch {
                        navigator.update(cmd.event)
                    }
                    for (updatable in appContainer.getEventUpdatables()) {
                        viewModelScope.launch {
                            updatable.update(cmd.event)
                        }
                    }
                }

                is RelayClosed -> viewModelScope.launch {
                    showSnackbarMsg(
                        appContainer.context.getString(
                            R.string.relay_closed,
                            cmd.relay,
                            cmd.msg
                        )
                    )
                }

                is RelayNotice -> viewModelScope.launch {
                    showSnackbarMsg(
                        appContainer.context.getString(
                            R.string.relay_msg,
                            cmd.relay,
                            cmd.msg
                        )
                    )
                }
            }

            is DrawerViewCmd -> this@Core.bookmarkVM.drawerVM.handle(action = cmd)

            is ClickUpvote -> viewModelScope.launch {
                val result = appContainer.eventCreator.publishVote(cmd.event)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_upvote))
                }
                // TODO: Show relay success when user enables it in settings
            }

            is ClickNeutralizeVotes -> viewModelScope.launch {
                val ids = appContainer.upvoteProvider.upvotes(cmd.event.id())
                val result = appContainer.eventCreator.publishDelete(eventIds = ids.toList())
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_deletion))
                }
            }

            is FollowProfile -> viewModelScope.launch {
                val result = appContainer.eventCreator.followProfile(cmd.pubkey)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_contact_list))
                }
            }

            is UnfollowProfile -> viewModelScope.launch {
                val result = appContainer.eventCreator.unfollowProfile(cmd.pubkey)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_contact_list))
                }
            }

            is FollowTopic -> viewModelScope.launch {
                val result = appContainer.eventCreator.followTopic(cmd.topic)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_topic_list))
                }
            }

            is UnfollowTopic -> viewModelScope.launch {
                val result = appContainer.eventCreator.unfollowTopic(cmd.topic)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_topic_list))
                }
            }

            is DeletePost -> viewModelScope.launch {
                val result = appContainer.eventCreator.publishDelete(listOf(cmd.event.id()))
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_deletion))
                }
            }

            is DeleteList -> viewModelScope.launch {
                val result = appContainer.eventCreator.publishListDeletion(cmd.ident)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_deletion))
                }
                // TODO: Close drawer
            }

            is AddPubkeyToList -> viewModelScope.launch {
                val result = appContainer.eventCreator.addPubkeyToList(cmd.pubkey, cmd.ident)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_profile_list))
                }
            }

            is AddTopicToList -> viewModelScope.launch {
                val result = appContainer.eventCreator.addTopicToList(cmd.topic, cmd.ident)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_topic_list))
                }
            }

            is BookmarkPost -> viewModelScope.launch {
                val result = appContainer.eventCreator.addBookmark(cmd.event.id())
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_bookmarks))
                }
            }

            is UnbookmarkPost -> viewModelScope.launch {
                val result = appContainer.eventCreator.removeBookmark(cmd.event.id())
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_bookmarks))
                }
            }

            is Rebroadcast -> viewModelScope.launch {
                val result = appContainer.service.rebroadcast(cmd.event)
                val success = result.success.size
                val total = result.failed.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.rebroadcasted_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is HomeViewCmd -> this@Core.bookmarkVM.homeVM.handle(action = cmd)
            is DiscoverViewCmd -> this@Core.bookmarkVM.discoverVM.handle(action = cmd)
            is ThreadViewCmd -> this@Core.bookmarkVM.threadVM.handle(action = cmd)
            is TopicViewCmd -> this@Core.bookmarkVM.topicVM.handle(action = cmd)
            is ProfileViewCmd -> this@Core.bookmarkVM.profileVM.handle(action = cmd)
            is SettingsViewCmd -> this@Core.bookmarkVM.settingsVM.handle(action = cmd)
            is CreatePostViewCmd -> this@Core.bookmarkVM.createPostVM.handle(action = cmd)
            is CreateGitIssueViewCmd -> this@Core.bookmarkVM.createGitIssueVM.handle(action = cmd)
            is CreateReplyViewCmd -> this@Core.bookmarkVM.createReplyVM.handle(action = cmd)
            is CrossPostViewCmd -> this@Core.bookmarkVM.createCrossPostVM.handle(action = cmd)
            is SearchViewCmd -> this@Core.bookmarkVM.searchVM.handle(action = cmd)
            is EditProfileViewCmd -> this@Core.bookmarkVM.editProfileVM.handle(action = cmd)
            is RelayEditorViewCmd -> this@Core.bookmarkVM.relayEditorVM.handle(action = cmd)
            is InboxViewCmd -> this@Core.bookmarkVM.inboxVM.handle(action = cmd)
            is FollowListsViewCmd -> this@Core.bookmarkVM.followListsVM.handle(action = cmd)
            is BookmarkViewCmd -> this@Core.bookmarkVM.bookmarkVM.handle(cmd = cmd)
            is EditListViewCmd -> this@Core.bookmarkVM.editListVM.handle(action = cmd)
            is ListViewCmd -> this@Core.bookmarkVM.listVM.handle(action = cmd)
        }
    }

    private fun startRelayListener(channel: Channel<RelayNotificationCmd>) {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                handleCmd(channel.receive())
            }
        }.invokeOnCompletion { ex ->
            Log.w(logTag, "RelayListener terminated", ex)
            startRelayListener(channel)
        }
    }

    private suspend fun showSnackbarMsg(msg: String) {
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
