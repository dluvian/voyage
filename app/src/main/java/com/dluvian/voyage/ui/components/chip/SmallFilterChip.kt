package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn

@Composable
fun SmallFilterChip(
    onClick: Fn,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    label: ComposableContent = {},
    leadingIcon: ComposableContent = {},
    trailingIcon: ComposableContent = {},
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled = true)
) {
    FilterChip(
        modifier = modifier.height(AssistChipDefaults.Height.times(0.7f)),
        selected = isSelected,
        onClick = onClick,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        label = label,
        colors = colors,
        border = border,
    )
}
