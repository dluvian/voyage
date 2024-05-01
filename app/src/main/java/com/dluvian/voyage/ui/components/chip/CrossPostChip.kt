package com.dluvian.voyage.ui.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.CrossPostIcon

@Composable
fun CrossPostChip(onClick: Fn) {
    ActionChip(
        icon = CrossPostIcon,
        count = 0,
        description = stringResource(id = R.string.cross_post),
        onClick = onClick
    )
}
