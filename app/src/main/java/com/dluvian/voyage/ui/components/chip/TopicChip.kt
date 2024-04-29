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
    isEnabled: Boolean = true,
    isSelected: Boolean = false,
    heightRatio: Float? = null,
    trailingImageVector: ImageVector? = null,
) {
    SmallFilterChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(text = "#$topic") },
        isSelected = isSelected,
        isEnabled = isEnabled,
        heightRatio = heightRatio,
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
