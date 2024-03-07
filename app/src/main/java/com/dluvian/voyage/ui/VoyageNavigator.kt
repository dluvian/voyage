package com.dluvian.voyage.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.dluvian.voyage.core.BackPress
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigation.Home
import com.dluvian.voyage.ui.view.HomeView

@Composable
fun VoyageNavigator(core: Core) {
    val currentView by core.navigator.currentView
    BackHandler { core.onUIEvent(BackPress) }

    when (currentView) {
        is Home -> HomeView()
    }
}
