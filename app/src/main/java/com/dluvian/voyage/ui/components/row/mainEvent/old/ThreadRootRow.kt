package com.dluvian.voyage.ui.components.row.mainEvent.old

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.ui.components.row.mainEvent.MainEventRow

@Composable
fun ThreadRootRow(
    post: RootPost,
    isOp: Boolean,
    onUpdate: OnUpdate
) {
    MainEventRow(
        ctx = post,
        isOp = isOp,
        isThread = true,
        showAuthorName = true,
        onUpdate = onUpdate
    )
}
