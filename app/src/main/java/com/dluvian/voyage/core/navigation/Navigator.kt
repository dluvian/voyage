package com.dluvian.voyage.core.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf

class Navigator {
    private val navBackStack: MutableState<List<NavView>> = mutableStateOf(listOf(Home))
    val currentView by lazy { derivedStateOf { navBackStack.value.last() } }

    fun push(view: NavView) {
        synchronized(navBackStack) {
            val current = navBackStack.value
            if (current.last() == view) return

            navBackStack.value = current + view
        }
    }

    fun pop() {
        synchronized(navBackStack) {
            val current = navBackStack.value
            if (current.size <= 1) return
            navBackStack.value = current.dropLast(1)
        }
    }
}