package com.dluvian.voyage.ui.components.bar

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.iconButton.SendIconButton
import com.dluvian.voyage.ui.components.indicator.TopBarCircleProgressIndicator

@Composable
fun ContentCreationTopAppBar(
    showSendButton: Boolean,
    isSendingContent: Boolean,
    onSend: Fn,
    onUpdate: OnUpdate
) {
    GoBackTopAppBar(
        actions = {
            if (showSendButton && !isSendingContent) {
                SendIconButton(onSend = onSend)
            }
            if (isSendingContent) TopBarCircleProgressIndicator()
        },
        onUpdate = onUpdate
    )
}
