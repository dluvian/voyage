package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FeedCtx
import com.dluvian.voyage.model.ReplyViewOpen
import com.dluvian.voyage.model.ThreadReplyCtx
import com.dluvian.voyage.model.ThreadRootCtx
import com.dluvian.voyage.model.UICtx
import com.dluvian.voyage.ui.theme.CommentIcon

@Composable
fun CountedReplyButton(ctx: UICtx, modifier: Modifier = Modifier, onUpdate: (Cmd) -> Unit) {
    val replyCount = remember(ctx) {
        when (ctx) {
            is FeedCtx, is ThreadReplyCtx -> 0u
            is ThreadRootCtx -> ctx.replyCount
        }
    }
    CountedIconButton(
        modifier = modifier,
        count = replyCount,
        icon = CommentIcon,
        description = stringResource(id = R.string.comment),
        onClick = { onUpdate(ReplyViewOpen(ctx.uiEvent)) },
    )
}
