package com.dluvian.voyage

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.model.BookmarkPost
import com.dluvian.voyage.model.BookmarkViewCmd
import com.dluvian.voyage.model.ClickNeutralizeVotes
import com.dluvian.voyage.model.ClickProfileSuggestion
import com.dluvian.voyage.model.ClickUpvote
import com.dluvian.voyage.model.CloseEventDetails
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.CoreActionCmd
import com.dluvian.voyage.model.CrossPostViewCmd
import com.dluvian.voyage.model.DeletePost
import com.dluvian.voyage.model.DiscoverViewCmd
import com.dluvian.voyage.model.DrawerViewCmd
import com.dluvian.voyage.model.EditProfileViewCmd
import com.dluvian.voyage.model.FollowListsViewCmd
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowTopic
import com.dluvian.voyage.model.GitIssueCmd
import com.dluvian.voyage.model.HomeViewCmd
import com.dluvian.voyage.model.InboxViewCmd
import com.dluvian.voyage.model.NavCmd
import com.dluvian.voyage.model.PostViewCmd
import com.dluvian.voyage.model.ProfileViewCmd
import com.dluvian.voyage.model.PublishNip65
import com.dluvian.voyage.model.PublishProfile
import com.dluvian.voyage.model.Rebroadcast
import com.dluvian.voyage.model.ReceiveEvent
import com.dluvian.voyage.model.RelayClosed
import com.dluvian.voyage.model.RelayEditorViewCmd
import com.dluvian.voyage.model.RelayNotice
import com.dluvian.voyage.model.RelayNotificationCmd
import com.dluvian.voyage.model.RelayProfileViewCmd
import com.dluvian.voyage.model.ReplyViewCmd
import com.dluvian.voyage.model.SearchProfileSuggestion
import com.dluvian.voyage.model.SearchTopicSuggestion
import com.dluvian.voyage.model.SearchViewCmd
import com.dluvian.voyage.model.SendCrossPost
import com.dluvian.voyage.model.SendGitIssue
import com.dluvian.voyage.model.SendPost
import com.dluvian.voyage.model.SendReply
import com.dluvian.voyage.model.SettingsViewCmd
import com.dluvian.voyage.model.ShowEventDetails
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
        viewModelScope.launch(Dispatchers.IO) {
            appContainer.service.init()
            appContainer.nameProvider.init()
            appContainer.trustProvider.init()
            appContainer.topicProvider.init()
            appContainer.upvoteProvider.init()
            appContainer.bookmarkProvider.init()
        }.invokeOnCompletion { Log.i(logTag, "Finished initializing", it) }
        viewModelScope.launch(Dispatchers.IO) {
            appContainer.service.handleNotifications()
        }
    }

    private fun handleCmd(cmd: Cmd) {
        when (cmd) {
            is NavCmd -> navigator.handle(cmd = cmd)

            is CoreActionCmd -> handleCoreAction(cmd)

            is RelayNotificationCmd -> when (cmd) {
                is ReceiveEvent -> {
                    // TODO: actor in navigator to prevent spamming launch
                    viewModelScope.launch(Dispatchers.IO) {
                        navigator.update(cmd.event)
                    }
                    for (updatable in appContainer.getEventUpdatables()) {
                        viewModelScope.launch(Dispatchers.IO) {
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

                is RelayNotice -> viewModelScope.launch(Dispatchers.IO) {
                    showSnackbarMsg(
                        appContainer.context.getString(
                            R.string.relay_msg,
                            cmd.relay,
                            cmd.msg
                        )
                    )
                }
            }

            is DrawerViewCmd -> vmContainer.drawerVM.handle(cmd)

            is HomeViewCmd -> vmContainer.homeVM.handle(cmd)
            is DiscoverViewCmd -> vmContainer.discoverVM.handle(cmd)
            is ThreadViewCmd -> vmContainer.threadVM.handle(cmd)
            is TopicViewCmd -> vmContainer.topicVM.handle(cmd)
            is ProfileViewCmd -> vmContainer.profileVM.handle(cmd)
            is SettingsViewCmd -> vmContainer.settingsVM.handle(cmd)
            is PostViewCmd -> vmContainer.postVM.handle(cmd)
            is GitIssueCmd -> vmContainer.gitIssueVM.handle(cmd)
            is ReplyViewCmd -> vmContainer.replyVM.handle(cmd)
            is CrossPostViewCmd -> vmContainer.crossPostVM.handle(cmd)
            is SearchViewCmd -> vmContainer.searchVM.handle(cmd)
            is EditProfileViewCmd -> vmContainer.editProfileVM.handle(cmd)
            is RelayEditorViewCmd -> vmContainer.relayEditorVM.handle(cmd)
            is InboxViewCmd -> vmContainer.inboxVM.handle(cmd)
            is FollowListsViewCmd -> vmContainer.followListsVM.handle(cmd)
            is BookmarkViewCmd -> vmContainer.bookmarkVM.handle(cmd)
            is RelayProfileViewCmd -> vmContainer.relayProfileVM.handle(cmd)
        }
    }

    private fun handleCoreAction(cmd: CoreActionCmd) {
        when (cmd) {
            is ClickUpvote -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishVote(cmd.event)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_upvote))
                }
            }

            is ClickNeutralizeVotes -> viewModelScope.launch(Dispatchers.IO) {
                val ids = appContainer.upvoteProvider.upvotes(cmd.event.id())
                val result = appContainer.eventCreator.publishDelete(eventIds = ids.toList())
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_deletion))
                }
            }

            is FollowProfile -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.followProfile(cmd.pubkey)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_contact_list))
                }
            }

            is UnfollowProfile -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.unfollowProfile(cmd.pubkey)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_contact_list))
                }
            }

            is FollowTopic -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.followTopic(cmd.topic)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_topic_list))
                }
            }

            is UnfollowTopic -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.unfollowTopic(cmd.topic)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_topic_list))
                }
            }

            is SendCrossPost -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishCrossPost(cmd.event, cmd.topics)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_cross_post))
                }
                val output = result.getOrNull() ?: return@launch
                val success = output.success.size
                val total = output.failed.keys.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.published_cross_post_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is DeletePost -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishDelete(listOf(cmd.event.id()))
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_deletion))
                }
            }

            is BookmarkPost -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.addBookmark(cmd.id)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_bookmarks))
                }
            }

            is UnbookmarkPost -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.removeBookmark(cmd.id)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_bookmarks))
                }
            }

            is Rebroadcast -> viewModelScope.launch(Dispatchers.IO) {
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

            is SendGitIssue -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishGitIssue(
                    repoCoord = VOYAGE_REPO_COORDINATE,
                    subject = cmd.header,
                    content = cmd.content,
                    label = cmd.type.label()
                )
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_git_issue))
                }
                val output = result.getOrNull() ?: return@launch
                val success = output.success.size
                val total = output.failed.keys.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.published_git_issue_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is SendPost -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishPost(
                    subject = cmd.subject,
                    content = cmd.content,
                    topics = cmd.topics
                )
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_post))
                }
                val output = result.getOrNull() ?: return@launch
                val success = output.success.size
                val total = output.failed.keys.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.published_post_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is SendReply -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishReply(cmd.content, cmd.parent)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_reply))
                }
                val output = result.getOrNull() ?: return@launch
                val success = output.success.size
                val total = output.failed.keys.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.published_reply_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is PublishNip65 -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishNip65(cmd.relays)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_relay_list))
                }
                val output = result.getOrNull() ?: return@launch
                val success = output.success.size
                val total = output.failed.keys.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.published_relay_list_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is PublishProfile -> viewModelScope.launch(Dispatchers.IO) {
                val result = appContainer.eventCreator.publishProfile(cmd.metadata)
                if (result.isFailure) {
                    showSnackbarMsg(appContainer.context.getString(R.string.failed_to_sign_profile))
                }
                val output = result.getOrNull() ?: return@launch
                val success = output.success.size
                val total = output.failed.keys.size + success
                showSnackbarMsg(
                    appContainer.context.getString(
                        R.string.published_profile_to_n_of_m_relays,
                        success,
                        total
                    )
                )
            }

            is ClickProfileSuggestion -> TODO()
            is SearchProfileSuggestion -> TODO()
            is SearchTopicSuggestion -> TODO()
            is ShowEventDetails -> TODO()
            is CloseEventDetails -> TODO()
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
        viewModelScope.launch(Dispatchers.Main) {
            appContainer.service.close()
        }
        super.onCleared()
    }
}
