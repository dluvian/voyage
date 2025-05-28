package com.dluvian.voyage.ui.components.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.ContentCreationTopAppBar


@Composable
fun ContentCreationScaffold(
    showSendButton: Boolean,
    isSendingContent: Boolean,
    snackbar: SnackbarHostState,
    title: String? = null,
    onSend: Fn,
    onUpdate: OnUpdate,
    content: ComposableContent,
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            ContentCreationTopAppBar(
                showSendButton = showSendButton,
                isSendingContent = isSendingContent,
                title = title,
                onSend = onSend,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
