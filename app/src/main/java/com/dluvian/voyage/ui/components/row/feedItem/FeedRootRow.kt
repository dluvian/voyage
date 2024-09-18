package com.dluvian.voyage.ui.components.row.feedItem

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPostUI

@Composable
fun FeedRootRow(post: RootPostUI, showAuthorName: Boolean, onUpdate: OnUpdate) {
    FeedItemRow(
        feedItem = post,
        isOp = false,
        isThread = false,
        showAuthorName = showAuthorName,
        onUpdate = onUpdate
    )
}
