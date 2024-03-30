package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.VMContainer
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.NavEvent
import com.dluvian.voyage.core.PopNavEvent
import com.dluvian.voyage.core.PushNavEvent

class Navigator(private val vmContainer: VMContainer, private val closeApp: Fn) {
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
                    is ThreadNavView -> vmContainer.threadVM.openThread(rootPost = navView.rootPost)
                    is ProfileNavView -> vmContainer.profileVM.openProfile(profileNavView = navView)
                    is TopicNavView -> vmContainer.topicVM.openTopic(topicNavView = navView)
                    is CommentCreationNavView -> vmContainer.createResponseVM.openParent(parent = navView.rootPost)
                    is ReplyCreationNavView -> vmContainer.createResponseVM.openParent(parent = navView.comment)
                }
            }

            is MainNavView -> { /* Do nothing */
            }

            is SimpleNonMainNavView -> { /* Do nothing */
            }
        }
    }
}
