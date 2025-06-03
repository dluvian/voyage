package com.dluvian.voyage.navigator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.navigator.AdvancedNonMainNavView
import com.dluvian.voyage.core.navigator.BookmarksNavView
import com.dluvian.voyage.core.navigator.CreateGitIssueNavView
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.CrossPostCreationNavView
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.EditExistingListNavView
import com.dluvian.voyage.core.navigator.EditNewListNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.FollowListsNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.core.navigator.NProfileNavView
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.OpenListNavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.RelayEditorNavView
import com.dluvian.voyage.core.navigator.RelayProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.SimpleNonMainNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.ThreadNeventNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.model.NavCmd
import com.dluvian.voyage.model.PopNavCmd
import com.dluvian.voyage.model.PushNavCmd
import com.dluvian.voyage.provider.IEventUpdate
import com.dluvian.voyage.viewModel.VMContainer
import rust.nostr.sdk.Event

class Navigator(private val vmContainer: VMContainer, private val closeApp: () -> Unit) :
    IEventUpdate {
    val stack = mutableStateOf<List<NavView>>(listOf(HomeNavView))

    fun handle(cmd: NavCmd) {
        when (cmd) {
            is PopNavCmd -> pop()
            is PushNavCmd -> push(view = cmd.getNavView())
        }
    }

    private fun push(view: NavView) {
        synchronized(stack) {
            val current = stack.value
            if (current.last() == view) return

            stack.value = current + view
            handleNavView(navView = view)
        }
    }

    private fun pop() {
        synchronized(stack) {
            val current = stack.value
            if (current.size <= 1) closeApp()
            else {
                stack.value = current.dropLast(1)
                handleNavView(navView = stack.value.last())
            }
        }
    }

    private fun handleNavView(navView: NavView) {
        when (navView) {
            is AdvancedNonMainNavView -> {
                when (navView) {
                    // TODO: Do push and pop functions for each viewmodel
                    is ThreadNavView -> vmContainer.threadVM.openThread(navView.event)
                    is ThreadNeventNavView -> vmContainer.threadVM.openNeventThread(navView.nevent)
                    is ProfileNavView -> vmContainer.profileVM.openProfile(navView.profileEvent)
                    is NProfileNavView -> vmContainer.profileVM.openNProfile(navView.nprofile)
                    is TopicNavView -> vmContainer.topicVM.openTopic(navView.topic)
                    is ReplyCreationNavView -> vmContainer.createReplyVM.openParent(navView.parent)
                    is CrossPostCreationNavView -> vmContainer.createCrossPostVM.prepareCrossPost(
                        navView.event
                    )
                    is RelayProfileNavView -> vmContainer.relayProfileVM.openProfile(relayUrl = navView.relayUrl)
                    is OpenListNavView -> vmContainer.listVM.openList(identifier = navView.identifier)
                    is EditExistingListNavView -> vmContainer.editListVM.editExisting(identifier = navView.identifier)

                    EditNewListNavView -> vmContainer.editListVM.createNew()
                }
            }

            is MainNavView -> { /* Do nothing */
            }

            is SimpleNonMainNavView -> { /* Do nothing */
            }
        }
    }

    override suspend fun update(event: Event) {
        // Only update what is on screen
        // Rest receive update via pop/push
        when (stack.value.last()) {
            DiscoverNavView -> vmContainer.discoverVM.update(event)
            HomeNavView -> vmContainer.homeVM.update(event)
            InboxNavView -> vmContainer.inboxVM.update(event)
            SearchNavView -> vmContainer.searchVM.update(event)
            is CrossPostCreationNavView -> vmContainer.createCrossPostVM.update(event)
            is EditExistingListNavView -> vmContainer.editListVM.update(event)
            EditNewListNavView -> vmContainer.listVM.update(event)
            is NProfileNavView -> vmContainer.profileVM.update(event)
            is OpenListNavView -> vmContainer.listVM.update(event)
            is ProfileNavView -> vmContainer.profileVM.update(event)
            is RelayProfileNavView -> vmContainer.relayProfileVM.update(event)
            is ReplyCreationNavView -> vmContainer.createReplyVM.update(event)
            is ThreadNavView -> vmContainer.threadVM.update(event)
            is ThreadNeventNavView -> vmContainer.threadVM.update(event)
            is TopicNavView -> vmContainer.topicVM.update(event)
            BookmarksNavView -> vmContainer.bookmarksVM.update(event)
            CreateGitIssueNavView -> vmContainer.createGitIssueVM.update(event)
            CreatePostNavView -> vmContainer.createPostVM.update(event)
            EditProfileNavView -> vmContainer.editProfileVM.update(event)
            FollowListsNavView -> vmContainer.followListsVM.update(event)
            RelayEditorNavView -> vmContainer.relayEditorVM.update(event)
            SettingsNavView -> vmContainer.settingsVM.update(event)
        }
    }
}
