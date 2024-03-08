package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Lambda
import com.dluvian.voyage.ui.theme.CommentIcon
import com.dluvian.voyage.ui.theme.RoundedChip

@Composable
fun CommentButton(commentCount: Int, onClick: Lambda) {
    AssistChip(
        modifier = Modifier.height(AssistChipDefaults.Height.times(0.8f)),
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
