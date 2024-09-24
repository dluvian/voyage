package com.dluvian.voyage.ui.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.CommentIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CommentChip(commentCount: Int, onClick: Fn) {
    ActionChip(
        icon = CommentIcon,
        description = stringResource(id = R.string.comment),
        count = commentCount,
        onClick = onClick,
        topPadding = spacing.small
    )
}
