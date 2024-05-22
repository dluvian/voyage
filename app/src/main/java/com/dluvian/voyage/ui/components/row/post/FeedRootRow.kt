package com.dluvian.voyage.ui.components.row.post

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPostUI

@Composable
fun FeedRootRow(post: RootPostUI, onUpdate: OnUpdate) {
    BaseRootRow(post = post, isOp = false, isThread = false, onUpdate = onUpdate)
}
