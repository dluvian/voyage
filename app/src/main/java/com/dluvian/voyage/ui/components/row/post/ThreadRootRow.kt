package com.dluvian.voyage.ui.components.row.post

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPostUI

@Composable
fun ThreadRootRow(
    post: RootPostUI,
    isOp: Boolean,
    onUpdate: OnUpdate
) {
    BaseRootRow(post = post, isOp = isOp, isThread = true, onUpdate = onUpdate)
}
