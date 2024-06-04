package com.dluvian.voyage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.RegisterAccountLauncher
import com.dluvian.voyage.core.RegisterSignerLauncher
import com.dluvian.voyage.core.getAccountLauncher
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.ui.views.main.MainView
import com.dluvian.voyage.ui.views.nonMain.NonMainView

@Composable
fun VoyageAppContent(core: Core) {
    // Don't register in MainActivity because it doesn't work there after toggling dark mode
    val signerLauncher = getSignerLauncher(onUpdate = core.onUpdate)
    val reqAccountLauncher = getAccountLauncher(onUpdate = core.onUpdate)
    core.onUpdate(RegisterSignerLauncher(launcher = signerLauncher))
    core.onUpdate(RegisterAccountLauncher(launcher = reqAccountLauncher))

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
