package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.button.SendIconButton
import com.dluvian.voyage.ui.components.indicator.SmallCircleProgressIndicator
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ContentCreationTopAppBar(
    showSendButton: Boolean,
    isSendingContent: Boolean,
    title: String? = null,
    typeIcon: ComposableContent = {},
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
            typeIcon()
            if (showSendButton && !isSendingContent) {
                SendIconButton(onSend = onSend)
            }
            if (isSendingContent) {
                SmallCircleProgressIndicator()
                Spacer(modifier = Modifier.padding(start = spacing.small))
            }
        },
        onUpdate = onUpdate
    )
}
