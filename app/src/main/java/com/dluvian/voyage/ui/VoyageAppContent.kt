package com.dluvian.voyage.ui

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigation.MainNavView
import com.dluvian.voyage.core.navigation.NonMainNavView
import com.dluvian.voyage.ui.views.main.MainView
import com.dluvian.voyage.ui.views.nonMain.NonMainView

@Composable
fun VoyageAppContent(core: Core) {
    when (val currentView = core.navigator.stack.value.last()) {
        is MainNavView -> MainView(
            homeViewModel = core.homeViewModel,
            currentView = currentView,
            snackbarHostState = core.snackbarHostState,
            onUpdate = core.onUpdate
        )

        is NonMainNavView -> NonMainView(
            currentView = currentView,
            snackbarHostState = core.snackbarHostState,
            onUpdate = core.onUpdate
        )
    }


}
