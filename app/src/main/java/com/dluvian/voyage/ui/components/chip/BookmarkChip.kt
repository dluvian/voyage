package com.dluvian.voyage.ui.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.BookmarkIcon

@Composable
fun BookmarkChip(onClick: Fn) {
    ActionChip(
        icon = BookmarkIcon,
        description = stringResource(id = R.string.remove_bookmark),
        count = 0,
        onClick = onClick
    )
}
