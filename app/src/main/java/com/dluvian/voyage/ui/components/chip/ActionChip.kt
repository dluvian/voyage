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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.RoundedChip
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ActionChip(
    icon: ImageVector,
    description: String,
    count: Int = 0,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: Fn,
    topPadding: Dp = 0.dp
) {
    Row(
        modifier = Modifier
            .clip(RoundedChip)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.large)
            .padding(top = topPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .height(AssistChipDefaults.Height.times(0.6f))
                .padding(horizontal = spacing.small),
            imageVector = icon,
            contentDescription = description,
            tint = tint
        )
        if (count > 0) {
            Text(
                modifier = Modifier.padding(horizontal = spacing.small),
                text = count.toString()
            )
        }
    }
}
