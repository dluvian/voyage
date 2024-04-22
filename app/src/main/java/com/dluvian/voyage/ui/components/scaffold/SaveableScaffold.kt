package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.iconButton.SaveIconButton
import com.dluvian.voyage.ui.components.indicator.TopBarCircleProgressIndicator

@Composable
fun SaveableScaffold(
    showSaveButton: Boolean,
    isSaving: Boolean,
    snackbar: SnackbarHostState,
    title: String? = null,
    onSave: Fn,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            GoBackTopAppBar(
                title = { if (title != null) Text(text = title) },
                actions = {
                    if (showSaveButton && !isSaving) {
                        SaveIconButton(onSave = onSave)
                    }
                    if (isSaving) TopBarCircleProgressIndicator()
                },
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
