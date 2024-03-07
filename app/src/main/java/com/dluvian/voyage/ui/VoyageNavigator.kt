package com.dluvian.voyage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigation.Home
import com.dluvian.voyage.ui.view.HomeView

@Composable
fun VoyageNavigator(core: Core) {
    val currentView by core.navigator.currentView
    when (currentView) {
        is Home -> HomeView()
    }
}
