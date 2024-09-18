package com.dluvian.voyage.ui.components.row.feedItem

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.LegacyReplyUI

@Composable
fun FeedReplyRow(
    reply: LegacyReplyUI,
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
