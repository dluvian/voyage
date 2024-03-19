package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.bar.MainBottomBar
import com.dluvian.voyage.ui.components.bar.VoyageTopAppBar

@Composable
fun MainScaffold(
    currentView: MainNavView,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            VoyageTopAppBar(
                title = currentView.getTitle(),
                hasSearch = true,
                onUpdate = onUpdate
            )
        },
        bottomBar = {
            MainBottomBar(
                currentView = currentView,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
