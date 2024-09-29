package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.dluvian.voyage.core.Fn

@Composable
fun CountedIconButton(
    count: Int,
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    onClick: Fn
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FooterIconButton(icon = icon, description = description, onClick = onClick)
        if (count > 0) Text(text = count.toString(), color = MaterialTheme.colorScheme.onBackground)
    }
}
