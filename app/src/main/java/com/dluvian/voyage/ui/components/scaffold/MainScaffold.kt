package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.ui.components.bar.MainBottomBar
import com.dluvian.voyage.ui.components.bar.MainTopAppBar
import com.dluvian.voyage.ui.views.nonMain.search.SearchScaffold

@Composable
fun MainScaffold(
    currentView: MainNavView,
    snackbar: SnackbarHostState,
    homeFeedState: LazyListState,
    inboxFeedState: LazyListState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    when (currentView) {
        HomeNavView, InboxNavView, DiscoverNavView -> {
            VoyageScaffold(
                snackbar = snackbar,
                topBar = {
                    MainTopAppBar(currentView = currentView, onUpdate = onUpdate)
                },
                bottomBar = {
                    MainBottomBar(
                        currentView = currentView,
                        homeFeedState = homeFeedState,
                        inboxFeedState = inboxFeedState,
                        onUpdate = onUpdate
                    )
                }
            ) {
                content()
            }
        }

        SearchNavView -> SearchScaffold(
            snackbar = snackbar,
            bottomBar = {
                MainBottomBar(
                    currentView = currentView,
                    homeFeedState = homeFeedState,
                    inboxFeedState = inboxFeedState,
                    onUpdate = onUpdate
                )
            },
            onUpdate = onUpdate
        ) {
            content()
        }
    }

}
