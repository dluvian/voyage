package com.dluvian.voyage.ui.components.row.mainEvent.old

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.LegacyReply

@Composable
fun FeedReplyRow(
    reply: LegacyReply,
    showAuthorName: Boolean,
    onUpdate: OnUpdate,
) {
    BaseReplyRow(
        reply = reply,
        isCollapsed = false,
        showFullReplyButton = true,
        isOp = false,
        isThread = false,
        showAuthorName = showAuthorName,
        onUpdate = onUpdate
    )
}
