package com.dluvian.voyage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import com.dluvian.voyage.Core
import com.dluvian.voyage.RegisterAccountLauncher
import com.dluvian.voyage.RegisterSignerLauncher
import com.dluvian.voyage.RegisterUriHandler
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.core.utils.getAccountLauncher
import com.dluvian.voyage.core.utils.getSignerLauncher
import com.dluvian.voyage.ui.views.main.MainView
import com.dluvian.voyage.ui.views.nonMain.NonMainView

@Composable
fun VoyageAppContent(core: Core) {
    // Don't register in MainActivity because it doesn't work there after toggling dark mode
    val signerLauncher = getSignerLauncher(onUpdate = core.onUpdate)
    val reqAccountLauncher = getAccountLauncher(onUpdate = core.onUpdate)
    val uriHandler = LocalUriHandler.current
    core.onUpdate(RegisterSignerLauncher(launcher = signerLauncher))
    core.onUpdate(RegisterAccountLauncher(launcher = reqAccountLauncher))
    core.onUpdate(RegisterUriHandler(uriHandler = uriHandler))

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
