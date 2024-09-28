package com.dluvian.voyage.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenReplyCreation
import com.dluvian.voyage.ui.components.row.mainEvent.MainEventCtx
import com.dluvian.voyage.ui.theme.ReplyIcon

@Composable
fun ReplyIconButton(ctx: MainEventCtx, onUpdate: OnUpdate) {
    FooterIconButton(
        icon = ReplyIcon,
        description = stringResource(id = R.string.reply),
        onClick = { onUpdate(OpenReplyCreation(parent = ctx.mainEvent)) })
}
