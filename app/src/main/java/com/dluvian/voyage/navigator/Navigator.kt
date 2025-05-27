package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.NavEvent
import com.dluvian.voyage.PopNavEvent
import com.dluvian.voyage.PushNavEvent
import com.dluvian.voyage.viewModel.VMContainer

class Navigator(private val vmContainer: VMContainer) {
    val stack = mutableStateOf<List<NavView>>(listOf(HomeNavView))

    fun handle(action: NavEvent) {
        when (action) {
            is PopNavEvent -> pop()
            is PushNavEvent -> push(view = action.getNavView())
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
                    is ThreadNavView -> vmContainer.threadVM.openThread(
                        nevent = createNevent(hex = navView.mainEvent.id),
                        parentUi = navView.mainEvent
                    )

                    is ThreadRawNavView -> vmContainer.threadVM.openThread(
                        nevent = navView.nevent,
                        parentUi = navView.parent
                    )

                    is ProfileNavView -> vmContainer.profileVM.openProfile(profileNavView = navView)
                    is TopicNavView -> vmContainer.topicVM.openTopic(topicNavView = navView)
                    is ReplyCreationNavView -> vmContainer.createReplyVM.openParent(newParent = navView.parent)
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
