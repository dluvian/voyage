package com.dluvian.voyage.navigator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.model.CrossPostViewOpen
import com.dluvian.voyage.model.NavCmd
import com.dluvian.voyage.model.PopNavCmd
import com.dluvian.voyage.model.ProfileViewPopNprofile
import com.dluvian.voyage.model.ProfileViewPushNprofile
import com.dluvian.voyage.model.PushNavCmd
import com.dluvian.voyage.model.RelayProfileViewOpen
import com.dluvian.voyage.model.ReplyViewOpen
import com.dluvian.voyage.model.ThreadViewPopNevent
import com.dluvian.voyage.model.ThreadViewPopUIEvent
import com.dluvian.voyage.model.ThreadViewPushNevent
import com.dluvian.voyage.model.ThreadViewPushUIEvent
import com.dluvian.voyage.model.TopicViewPop
import com.dluvian.voyage.model.TopicViewPush
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
                is ThreadNavView -> {
                    val cmd = when (action) {
                        Pop -> ThreadViewPopUIEvent(navView.event)
                        Push -> ThreadViewPushUIEvent(navView.event)
                    }
                    vmContainer.threadVM.handle(cmd)
                }

                is ThreadNeventNavView -> {
                    val cmd = when (action) {
                        Pop -> ThreadViewPopNevent(navView.nevent)
                        Push -> ThreadViewPushNevent(navView.nevent)
                    }
                    vmContainer.threadVM.handle(cmd)
                }

                is ProfileNavView -> {
                    val cmd = when (action) {
                        Pop -> ProfileViewPopNprofile(navView.nprofile)
                        Push -> ProfileViewPushNprofile(navView.nprofile)
                    }
                    vmContainer.profileVM.handle(cmd)
                }

                is TopicNavView -> {
                    val cmd = when (action) {
                        Pop -> TopicViewPop(navView.topic)
                        Push -> TopicViewPush(navView.topic)
                    }
                    vmContainer.topicVM.handle(cmd)
                }

                is ReplyNavView -> {
                    vmContainer.replyVM.handle(ReplyViewOpen(navView.parent))
                }

                is CrossPostNavView -> vmContainer
                    .crossPostVM
                    .handle(CrossPostViewOpen(navView.event))

                is RelayProfileNavView -> vmContainer
                    .relayProfileVM
                    .handle(RelayProfileViewOpen(navView.relayUrl))
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
            DiscoverNavView -> vmContainer.discoverVM.update(event)
            HomeNavView -> vmContainer.homeVM.update(event)
            InboxNavView -> vmContainer.inboxVM.update(event)
            SearchNavView -> vmContainer.searchVM.update(event)
            is ProfileNavView -> vmContainer.profileVM.update(event)
            is ThreadNavView -> vmContainer.threadVM.update(event)
            is ThreadNeventNavView -> vmContainer.threadVM.update(event)
            is TopicNavView -> vmContainer.topicVM.update(event)
            BookmarkNavView -> vmContainer.bookmarkVM.update(event)
            FollowListsNavView -> vmContainer.followListsVM.update(event)
            RelayEditorNavView -> vmContainer.relayEditorVM.update(event)
            EditProfileNavView,
            is ReplyNavView,
            CreateGitIssueNavView,
            CreatePostNavView,
            SettingsNavView,
            is RelayProfileNavView,
            is CrossPostNavView -> {

            }
        }
    }
}
