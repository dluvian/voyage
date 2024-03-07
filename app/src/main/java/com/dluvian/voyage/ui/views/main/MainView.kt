package com.dluvian.voyage.ui.views.main

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.Upvote
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.MainNavView
import com.dluvian.voyage.core.navigation.TopicsNavView
import com.dluvian.voyage.ui.views.main.components.MainScaffold
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView
import com.dluvian.voyage.ui.views.main.subViews.TopicsView

@Composable
fun MainView(currentView: MainNavView, snackbarHostState: SnackbarHostState, onUpdate: OnUpdate) {
    MainScaffold(
        currentView = currentView,
        snackBarHostState = snackbarHostState,
        onUpdate = onUpdate
    ) {
        when (currentView) {
            is HomeNavView -> HomeView(
                listOf(
                    RootPost(
                        "id",
                        "pubkey",
                        "topic",
                        "time",
                        "title",
                        "content",
                        Upvote,
                        21,
                        69,
                        12,
                        5
                    )
                ), false, onUpdate = onUpdate
            )

            InboxNavView -> InboxView()
            TopicsNavView -> TopicsView()
        }
    }
}