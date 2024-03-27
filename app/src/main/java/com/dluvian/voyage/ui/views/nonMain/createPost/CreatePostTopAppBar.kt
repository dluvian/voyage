package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import com.dluvian.voyage.core.CreatePostViewSendPost
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.iconButton.SendIconButton
import com.dluvian.voyage.ui.components.indicator.TopBarCircleProgressIndicator

@Composable
fun CreatePostTopAppBar(
    header: State<String>,
    body: State<String>,
    isSendingPost: Boolean,
    onUpdate: OnUpdate
) {
    val context = LocalContext.current
    GoBackTopAppBar(
        actions = {
            if (body.value.isNotBlank() && !isSendingPost) {
                SendIconButton(onSend = {
                    onUpdate(
                        CreatePostViewSendPost(
                            header = header.value,
                            body = body.value,
                            context = context,
                            onGoBack = { onUpdate(GoBack) }
                        )
                    )
                })
            }
            if (isSendingPost) TopBarCircleProgressIndicator()
        },
        onUpdate = onUpdate
    )
}
