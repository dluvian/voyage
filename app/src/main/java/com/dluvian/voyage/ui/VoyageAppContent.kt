package com.dluvian.voyage.ui

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.ui.views.main.MainView
import com.dluvian.voyage.ui.views.nonMain.NonMainView

@Composable
fun VoyageAppContent(core: Core) {
    when (val currentView = core.navigator.stack.value.last()) {
        is MainNavView -> MainView(
            core = core,
            currentView = currentView,
        )

        is NonMainNavView -> NonMainView(
            core = core,
            currentView = currentView,
        )
    }
}
