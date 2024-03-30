package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold


@Composable
fun ContentCreationScaffold(
    showSendButton: Boolean,
    isSendingContent: Boolean,
    snackbar: SnackbarHostState,
    onSend: Fn,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            ContentCreationTopAppBar(
                showSendButton = showSendButton,
                isSendingContent = isSendingContent,
                onSend = onSend,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
