package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.ui.theme.FancyBrush

@Composable
fun TopicChip(topic: String, modifier: Modifier = Modifier) {
    SmallAssistChip(
        modifier = modifier,
        onClick = { },
        label = { Text(text = "#$topic") },
        border = BorderStroke(width = 1.dp, brush = FancyBrush),
    )
}
