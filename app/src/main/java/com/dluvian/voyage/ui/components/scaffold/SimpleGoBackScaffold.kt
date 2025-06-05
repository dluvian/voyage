package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar

Composable () ->Unit
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar

@Composable
fun SimpleGoBackScaffold(
    header: String,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: @Composable () -> Unit
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            SimpleGoBackTopAppBar(title = header, onUpdate = onUpdate)
        }
    ) {
        content()
    }
}
