package com.dluvian.voyage.ui.views.main

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.views.main.subViews.DiscoverView
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView

@Composable
fun MainView(
    core: Core,
    currentView: MainNavView,
) {
    MainScaffold(
        currentView = currentView,
        snackbar = core.appContainer.snackbar,
        onUpdate = core.onUpdate
    ) {
        when (currentView) {
            HomeNavView -> HomeView(vm = core.vmContainer.homeVM, onUpdate = core.onUpdate)
            DiscoverNavView -> DiscoverView(
                vm = core.vmContainer.discoverVM,
                onUpdate = core.onUpdate
            )
            InboxNavView -> InboxView()
        }
    }
}