package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.dluvian.voyage.core.CreatePostViewSendPost
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.iconButton.SendIconButton

@Composable
fun CreatePostTopAppBar(header: State<String>, body: State<String>, onUpdate: OnUpdate) {
    GoBackTopAppBar(
        actions = {
            if (body.value.isNotBlank()) {
                SendIconButton(onSend = {
                    onUpdate(CreatePostViewSendPost(header.value, body.value))
                })
            }
        },
        onUpdate = onUpdate
    )
}
