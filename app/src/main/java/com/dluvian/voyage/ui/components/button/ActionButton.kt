package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    isActive: Boolean,
    activeLabel: String,
    unactiveLabel: String,
    isEnabled: Boolean = true,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    Button(
        modifier = Modifier.height(ButtonDefaults.MinHeight),
        onClick = { if (isActive) onDeactivate() else onActivate() },
        enabled = isEnabled,
        colors = if (isActive) ButtonDefaults.buttonColors().copy(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
        else ButtonDefaults.buttonColors(),
        border = if (isActive) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onBackground
        ) else null
    ) {
        Text(text = if (isActive) activeLabel else unactiveLabel)
    }
}
