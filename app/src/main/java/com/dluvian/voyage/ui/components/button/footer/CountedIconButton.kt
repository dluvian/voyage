package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CountedIconButton(
    count: UInt,
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FooterIconButton(icon = icon, description = description, onClick = onClick)
        if (count > 0u) Text(text = count.toString())
    }
}
