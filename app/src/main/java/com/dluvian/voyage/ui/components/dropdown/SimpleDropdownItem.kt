package com.dluvian.voyage.ui.components.dropdown

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SimpleDropdownItem(text: String, onClick: () -> Unit, isEnabled: Boolean = true) {
    DropdownMenuItem(
        enabled = isEnabled,
        text = { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        onClick = onClick
    )
}
