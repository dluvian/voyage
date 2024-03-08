package com.dluvian.voyage.ui.views.main

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.MainNavView
import com.dluvian.voyage.core.navigation.TopicsNavView
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView
import com.dluvian.voyage.ui.views.main.subViews.TopicsView

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
            TopicsNavView -> TopicsView()
        }
    }
}