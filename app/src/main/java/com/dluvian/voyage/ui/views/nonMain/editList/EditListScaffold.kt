package com.dluvian.voyage.ui.views.nonMain.editList

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun EditListScaffold(
    title: MutableState<String>,
    isSaving: Boolean,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        if (title.value.isEmpty()) focusRequester.requestFocus()
    }

    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            EditListTopAppBar(
                title = title,
                isSaving = isSaving,
                focusRequester = focusRequester,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
