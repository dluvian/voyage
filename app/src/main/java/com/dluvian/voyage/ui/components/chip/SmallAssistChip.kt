package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn

@Composable
fun SmallAssistChip(
    onClick: Fn,
    modifier: Modifier = Modifier,
    label: ComposableContent = {},
    leadingIcon: ComposableContent = {},
    trailingIcon: ComposableContent = {},
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled = true)
) {
    AssistChip(
        modifier = modifier.height(AssistChipDefaults.Height.times(0.7f)),
        onClick = onClick,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        label = label,
        colors = colors,
        border = border,
    )
}
