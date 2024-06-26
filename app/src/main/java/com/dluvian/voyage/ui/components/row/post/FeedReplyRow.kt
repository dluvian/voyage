package com.dluvian.voyage.ui.components.row.post

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.ReplyUI

@Composable
fun FeedReplyRow(
    reply: ReplyUI,
    onUpdate: OnUpdate,
) {
    BaseReplyRow(
        reply = reply,
        isCollapsed = false,
        showFullReplyButton = true,
        isOp = false,
        isThread = false,
        onUpdate = onUpdate
    )
}
