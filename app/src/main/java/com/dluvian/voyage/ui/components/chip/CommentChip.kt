package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.CommentIcon
import com.dluvian.voyage.ui.theme.RoundedChip
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CommentChip(commentCount: Int, onClick: Fn) {
    AssistChip(
        modifier = Modifier
            .height(AssistChipDefaults.Height.times(0.8f))
            .padding(horizontal = spacing.large),
        onClick = onClick,
        leadingIcon = {
            Icon(
                modifier = Modifier.height(AssistChipDefaults.Height.times(0.5f)),
                imageVector = CommentIcon,
                contentDescription = stringResource(id = R.string.comment)
            )
        },
        shape = RoundedChip,
        label = { Text(text = "$commentCount ${stringResource(id = R.string.comments)}") },
    )
}
