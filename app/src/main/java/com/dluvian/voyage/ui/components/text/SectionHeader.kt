package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun SectionHeader(header: String) {
    Text(
        modifier = Modifier
            .padding(top = spacing.bigScreenEdge)
            .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.large),
        text = header,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold
    )
}
