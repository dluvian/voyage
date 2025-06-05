package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar

@Composable
fun SimpleGoBackScaffold(
    header: String,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit,
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
