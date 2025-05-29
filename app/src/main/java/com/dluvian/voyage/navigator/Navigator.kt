package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.NavCmd
import com.dluvian.voyage.PopNavCmd
import com.dluvian.voyage.PushNavCmd
import com.dluvian.voyage.viewModel.VMContainer

class Navigator(private val vmContainer: VMContainer, private val closeApp: () -> Unit) {
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
                    is CrossPostCreationNavView -> vmContainer.createCrossPostVM.prepareCrossPost(id = navView.id)
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
}
