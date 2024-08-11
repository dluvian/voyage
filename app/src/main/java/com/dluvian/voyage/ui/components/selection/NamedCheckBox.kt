package com.dluvian.voyage.ui.components.selection

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.ui.components.text.NamedItem

@Composable
fun NamedCheckbox(
    isChecked: Boolean,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    NamedItem(
        modifier = modifier,
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
