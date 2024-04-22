package com.dluvian.voyage.ui.components

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import com.dluvian.voyage.ui.components.text.NamedItem

@Composable
fun NamedCheckbox(
    isChecked: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    NamedItem(
        name = name,
        item = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onClick() },
                enabled = isEnabled,
            )
        },
    )
}
