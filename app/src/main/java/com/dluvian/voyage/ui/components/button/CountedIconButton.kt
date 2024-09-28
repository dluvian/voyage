package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.dluvian.voyage.core.Fn

@Composable
fun CountedIconButton(count: Int, icon: ImageVector, description: String, onClick: Fn) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FooterIconButton(icon = icon, description = description, onClick = onClick)
        if (count > 0) Text(text = count.toString())
    }
}
