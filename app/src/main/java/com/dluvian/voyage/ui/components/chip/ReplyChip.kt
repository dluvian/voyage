package com.dluvian.voyage.ui.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.ReplyIcon

@Composable
fun ReplyChip(onClick: Fn) {
    ActionChip(
        icon = ReplyIcon,
        description = stringResource(id = R.string.reply),
        onClick = onClick,
    )
}
