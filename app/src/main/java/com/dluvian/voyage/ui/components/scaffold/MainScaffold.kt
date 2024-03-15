package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.bar.MainBottomBar
import com.dluvian.voyage.ui.components.bar.VoyageTopAppBar

@Composable
fun MainScaffold(
    currentView: MainNavView,
    snackBarHostState: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    Scaffold(
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
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) {
        Box(modifier = Modifier.padding(it)) {
            content()
        }
    }
}
