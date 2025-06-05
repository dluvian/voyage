package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.bar.ContentCreationTopAppBar


@Composable
fun ContentCreationScaffold(
    showSendButton: Boolean,
    snackbar: SnackbarHostState,
    title: String? = null,
    onSend: () -> Unit,
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit,
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            ContentCreationTopAppBar(
                showSendButton = showSendButton,
                title = title,
                onSend = onSend,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
