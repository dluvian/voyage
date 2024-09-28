package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.light
import com.dluvian.voyage.ui.theme.sizing

@Composable
fun FooterIconButton(icon: ImageVector, description: String, onClick: Fn) {
    IconButton(
        modifier = Modifier.size(sizing.iconButton),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground.light(factor = 0.4f)
        ),
        onClick = onClick
    ) {
        Icon(imageVector = icon, contentDescription = description)
    }
}
