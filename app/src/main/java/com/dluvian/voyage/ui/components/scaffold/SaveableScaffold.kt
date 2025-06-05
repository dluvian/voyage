package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.button.SaveIconButton

@Composable
fun SaveableScaffold(
    showSaveButton: Boolean,
    snackbar: SnackbarHostState,
    title: String? = null,
    onSave: () -> Unit,
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            GoBackTopAppBar(
                title = { if (title != null) Text(text = title) },
                actions = {
                    if (showSaveButton) SaveIconButton(onSave = onSave)
                },
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
