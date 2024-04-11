package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.CommentIcon
import com.dluvian.voyage.ui.theme.RoundedChip
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CommentChip(commentCount: Int, onClick: Fn) {
    Row(
        modifier = Modifier
            .clip(RoundedChip)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .height(AssistChipDefaults.Height.times(0.6f))
                .padding(horizontal = spacing.small),
            imageVector = CommentIcon,
            contentDescription = stringResource(id = R.string.comment),
            tint = MaterialTheme.colorScheme.primary
        )
        if (commentCount > 0) {
            Text(
                modifier = Modifier.padding(horizontal = spacing.small),
                text = commentCount.toString()
            )
        }
    }
}
