package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold


@Composable
fun CreatePostScaffold(
    header: State<String>,
    body: State<String>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            CreatePostTopAppBar(
                body = body,
                header = header,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
