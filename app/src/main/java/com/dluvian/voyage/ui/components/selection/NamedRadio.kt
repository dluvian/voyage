package com.dluvian.voyage.ui.components.selection

import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.ui.components.text.NamedItem

@Composable
fun NamedRadio(
    isSelected: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    NamedItem(
        modifier = Modifier.clickable(onClick = onClick),
        name = name,
        item = {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                enabled = isEnabled,
            )
        },
    )
}
