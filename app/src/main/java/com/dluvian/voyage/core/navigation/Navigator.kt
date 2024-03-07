package com.dluvian.voyage.core.navigation

import androidx.compose.runtime.mutableStateOf

class Navigator : INavigator {
    override val stack = mutableStateOf<List<NavView>>(listOf(HomeNavView))

    override fun push(view: NavView) {
        synchronized(stack) {
            val current = stack.value
            if (current.last() == view) return

            stack.value = current + view
        }
    }

    override fun pop() {
        synchronized(stack) {
            val current = stack.value
            if (current.size <= 1) return
            stack.value = current.dropLast(1)
        }
    }
}