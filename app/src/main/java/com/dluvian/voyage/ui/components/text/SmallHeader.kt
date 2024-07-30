package com.dluvian.voyage.ui.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SmallHeader(header: String) {
    Text(text = header, style = MaterialTheme.typography.titleMedium)
}
