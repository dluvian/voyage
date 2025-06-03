package com.dluvian.voyage.ui.views.main

import androidx.compose.runtime.Composable
import com.dluvian.voyage.Core
import com.dluvian.voyage.navigator.DiscoverNavView
import com.dluvian.voyage.navigator.HomeNavView
import com.dluvian.voyage.navigator.InboxNavView
import com.dluvian.voyage.navigator.MainNavView
import com.dluvian.voyage.navigator.SearchNavView
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.views.main.subViews.DiscoverView
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView
import com.dluvian.voyage.ui.views.nonMain.search.SearchView
import com.dluvian.voyage.viewModel.VMContainer
import kotlinx.coroutines.CoroutineScope

@Composable
fun MainView(
    core: Core,
    scope: CoroutineScope,
    currentView: MainNavView,
) {
    MainDrawer(vm = VMContainer.drawerVM, scope = scope, onUpdate = core.onUpdate) {
        MainScaffold(
            currentView = currentView,
            snackbar = core.appContainer.snackbar,
            homeFeedState = VMContainer.homeVM.feedState,
            inboxFeedState = VMContainer.inboxVM.feedState,
            onUpdate = core.onUpdate
        ) {
            when (currentView) {
                HomeNavView -> HomeView(vm = VMContainer.homeVM, onUpdate = core.onUpdate)
                InboxNavView -> InboxView(vm = VMContainer.inboxVM, onUpdate = core.onUpdate)
                SearchNavView -> SearchView(
                    vm = VMContainer.searchVM,
                    onUpdate = core.onUpdate
                )

                DiscoverNavView -> DiscoverView(
                    vm = VMContainer.discoverVM,
                    onUpdate = core.onUpdate
                )
            }
        }
    }
}
