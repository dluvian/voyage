package com.dluvian.voyage.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.ui.theme.HashtagChipGradient

@Composable
fun TopicChip(topic: String, modifier: Modifier = Modifier) {
    AssistChip(
        modifier = modifier.height(AssistChipDefaults.Height.times(0.7f)),
        onClick = { },
        label = { Text(text = "#$topic") },
        border = BorderStroke(width = 1.dp, brush = Brush.linearGradient(HashtagChipGradient)),
    )
}
