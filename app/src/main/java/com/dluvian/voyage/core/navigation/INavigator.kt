package com.dluvian.voyage.core.navigation

import androidx.compose.runtime.State

interface INavigator {
    val stack: State<List<NavView>>
    fun push(view: NavView)
    fun pop()
}
