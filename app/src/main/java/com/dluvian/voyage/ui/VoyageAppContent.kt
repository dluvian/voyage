package com.dluvian.voyage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import com.dluvian.voyage.Core
import com.dluvian.voyage.navigator.MainNavView
import com.dluvian.voyage.navigator.NonMainNavView
import com.dluvian.voyage.ui.views.main.MainView
import com.dluvian.voyage.ui.views.nonMain.NonMainView

@Composable
fun VoyageAppContent(core: Core) {
    // Don't register in MainActivity because it doesn't work there after toggling dark mode
    val uriHandler = LocalUriHandler.current
    // TODO: Register URIHandler core.onUpdate(...)

    // Scope for closing drawer
    val scope = rememberCoroutineScope()

    when (val currentView = core.navigator.stack.value.last()) {
        is MainNavView -> MainView(
            core = core,
            scope = scope,
            currentView = currentView,
        )

        is NonMainNavView -> NonMainView(
            core = core,
            currentView = currentView,
        )
    }
}
