package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.button.SendIconButton

@Composable
fun ContentCreationTopAppBar(
    showSendButton: Boolean,
    title: String? = null,
    onSend: () -> Unit,
    onUpdate: (Cmd) -> Unit
) {
    GoBackTopAppBar(
        title = {
            if (title != null) Text(
                modifier = Modifier.basicMarquee(),
                text = title,
                maxLines = 1,
            )
        },
        actions = {
            if (showSendButton) SendIconButton(onSend = onSend)
        },
        onUpdate = onUpdate
    )
}
