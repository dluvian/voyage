package com.dluvian.voyage.ui.components.row.feedItem

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPostUI

@Composable
fun ThreadRootRow(
    post: RootPostUI,
    isOp: Boolean,
    onUpdate: OnUpdate
) {
    FeedItemRow(
        feedItem = post,
        isOp = isOp,
        isThread = true,
        showAuthorName = true,
        onUpdate = onUpdate
    )
}
