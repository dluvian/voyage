package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent

@Composable
fun VoyageScaffold(
    snackbar: SnackbarHostState,
    topBar: ComposableContent,
    bottomBar: ComposableContent = {},
    content: ComposableContent,
) {
    Scaffold(
        topBar = topBar,
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        bottomBar = bottomBar
    ) {
        Box(modifier = Modifier.padding(it)) {
            content()
        }
    }
}
