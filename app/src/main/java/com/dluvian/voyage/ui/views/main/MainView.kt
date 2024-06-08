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
import kotlinx.coroutines.CoroutineScope

@Composable
fun MainView(
    core: Core,
    scope: CoroutineScope,
    currentView: MainNavView,
) {
    MainDrawer(core = core, scope = scope) {
        MainScaffold(
            currentView = currentView,
            snackbar = core.appContainer.snackbar,
            homeFeedState = core.vmContainer.homeVM.feedState,
            inboxFeedState = core.vmContainer.inboxVM.feedState,
            onUpdate = core.onUpdate
        ) {
            when (currentView) {
                HomeNavView -> HomeView(vm = core.vmContainer.homeVM, onUpdate = core.onUpdate)
                InboxNavView -> InboxView(vm = core.vmContainer.inboxVM, onUpdate = core.onUpdate)
                DiscoverNavView -> DiscoverView(
                    vm = core.vmContainer.discoverVM,
                    onUpdate = core.onUpdate
                )
            }
        }
    }
}
