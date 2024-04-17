package com.dluvian.voyage.ui.views.nonMain.editProfile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun EditProfileScaffold(
    showSaveButton: Boolean,
    isSaving: Boolean,
    snackbar: SnackbarHostState,
    onSave: Fn,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            EditProfileTopAppBar(
                showSaveButton = showSaveButton,
                isSaving = isSaving,
                onSave = onSave,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
