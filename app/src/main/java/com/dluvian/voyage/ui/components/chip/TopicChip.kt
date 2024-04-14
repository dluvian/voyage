package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.FancyBrush

@Composable
fun TopicChip(
    topic: String,
    onClick: Fn,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    trailingImageVector: ImageVector? = null,
) {
    SmallFilterChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(text = "#$topic") },
        isSelected = isSelected,
        trailingIcon = {
            if (trailingImageVector != null) {
                Icon(
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                    imageVector = trailingImageVector,
                    contentDescription = null,
                )
            }
        },
        border = BorderStroke(width = 1.dp, brush = FancyBrush),
    )
}
