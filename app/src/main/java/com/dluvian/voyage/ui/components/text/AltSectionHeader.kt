package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun AltSectionHeader(header: String) {
    Text(
        modifier = Modifier
            .padding(horizontal = spacing.bigScreenEdge)
            .padding(top = spacing.xl, bottom = spacing.small),
        text = header,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
