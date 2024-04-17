package com.dluvian.voyage.ui.components.button

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.ui.components.icon.ExpandOrCollapseIcon

@Composable
fun ExpandToggleTextButton(
    text: String,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onToggle
    ) {
        Text(text = text)
        ExpandOrCollapseIcon(isExpanded = isExpanded)
    }
}
