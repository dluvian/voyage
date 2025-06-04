package com.dluvian.voyage.navigator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.model.BookmarkViewEventUpdate
import com.dluvian.voyage.model.DiscoverViewEventUpdate
import com.dluvian.voyage.model.HomeViewEventUpdate
import com.dluvian.voyage.model.NavCmd
import com.dluvian.voyage.model.OpenCrossPostView
import com.dluvian.voyage.model.PopNavCmd
import com.dluvian.voyage.model.PushNavCmd
import com.dluvian.voyage.provider.IEventUpdate
import com.dluvian.voyage.viewModel.VMContainer
import rust.nostr.sdk.Event

private sealed class NavAction()
private object Push : NavAction()
private object Pop : NavAction()

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
            handleNavView(navView = view, action = Push)
        }
    }

    private fun pop() {
        synchronized(stack) {
            val current = stack.value
            if (current.size <= 1) closeApp()
            else {
                stack.value = current.dropLast(1)
                handleNavView(navView = stack.value.last(), action = Pop)
            }
        }
    }

    private fun handleNavView(navView: NavView, action: NavAction) {
        when (navView) {
            is AdvancedNonMainNavView -> when (navView) {
                is ThreadNavView -> vmContainer.threadVM.openThread(navView.event)
                is ThreadNeventNavView -> vmContainer.threadVM.openNeventThread(navView.nevent)
                is ProfileNavView -> vmContainer.profileVM.openProfile(navView.profileEvent)
                is NProfileNavView -> vmContainer.profileVM.openNProfile(navView.nprofile)
                is TopicNavView -> vmContainer.topicVM.openTopic(navView.topic)
                is ReplyNavView -> vmContainer.createReplyVM.openParent(navView.parent)
                is CrossPostNavView -> vmContainer.createCrossPostVM.handle(
                    OpenCrossPostView(
                        navView.event
                    )
                )

                is RelayProfileNavView -> vmContainer.relayProfileVM.openProfile(relayUrl = navView.relayUrl)
            }

            is MainNavView -> when (navView) {
                DiscoverNavView -> TODO()
                HomeNavView -> TODO()
                InboxNavView -> TODO()
                SearchNavView -> TODO()
            }


            is SimpleNonMainNavView -> when (navView) {
                BookmarkNavView -> TODO()
                CreateGitIssueNavView -> TODO()
                CreatePostNavView -> TODO()
                EditProfileNavView -> TODO()
                FollowListsNavView -> TODO()
                RelayEditorNavView -> TODO()
                SettingsNavView -> TODO()
            }
        }
    }

    override suspend fun update(event: Event) {
        // Only update what is on screen
        // Rest receive update via pop/push
        when (stack.value.last()) {
            DiscoverNavView -> vmContainer.discoverVM.handle(DiscoverViewEventUpdate(event))
            HomeNavView -> vmContainer.homeVM.handle(HomeViewEventUpdate(event))
            InboxNavView -> vmContainer.inboxVM.update(event)
            SearchNavView -> vmContainer.searchVM.update(event)
            is NProfileNavView -> vmContainer.profileVM.update(event)
            is ProfileNavView -> vmContainer.profileVM.update(event)
            is RelayProfileNavView -> vmContainer.relayProfileVM.update(event)
            is ReplyNavView -> vmContainer.createReplyVM.update(event)
            is ThreadNavView -> vmContainer.threadVM.update(event)
            is ThreadNeventNavView -> vmContainer.threadVM.update(event)
            is TopicNavView -> vmContainer.topicVM.update(event)
            BookmarkNavView -> vmContainer.bookmarkVM.handle(BookmarkViewEventUpdate(event))
            CreateGitIssueNavView -> vmContainer.createGitIssueVM.update(event)
            CreatePostNavView -> vmContainer.createPostVM.update(event)
            EditProfileNavView -> vmContainer.editProfileVM.update(event)
            FollowListsNavView -> vmContainer.followListsVM.update(event)
            RelayEditorNavView -> vmContainer.relayEditorVM.update(event)
            SettingsNavView -> vmContainer.settingsVM.update(event)
            is CrossPostNavView -> {

            }
        }
    }
}
