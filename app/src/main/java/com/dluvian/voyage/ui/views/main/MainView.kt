package com.dluvian.voyage.ui.views.main

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.views.main.subViews.DiscoverView
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView

@Composable
fun MainView(
    homeViewModel: HomeViewModel,
    currentView: MainNavView,
    snackbarHostState: SnackbarHostState,
    onUpdate: OnUpdate
) {
    MainScaffold(
        currentView = currentView,
        snackBarHostState = snackbarHostState,
        onUpdate = onUpdate
    ) {
        when (currentView) {
            is HomeNavView -> HomeView(vm = homeViewModel, onUpdate = onUpdate)

            InboxNavView -> InboxView()
            DiscoverNavView -> DiscoverView()
        }
    }
}