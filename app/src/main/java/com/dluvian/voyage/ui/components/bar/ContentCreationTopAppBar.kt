package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.button.SendIconButton
import com.dluvian.voyage.ui.components.indicator.TopBarCircleProgressIndicator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentCreationTopAppBar(
    showSendButton: Boolean,
    isSendingContent: Boolean,
    title: String? = null,
    onSend: Fn,
    onUpdate: OnUpdate
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
            if (showSendButton && !isSendingContent) {
                SendIconButton(onSend = onSend)
            }
            if (isSendingContent) TopBarCircleProgressIndicator()
        },
        onUpdate = onUpdate
    )
}
