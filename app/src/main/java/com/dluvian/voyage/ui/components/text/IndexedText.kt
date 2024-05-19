package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun IndexedText(
    modifier: Modifier = Modifier,
    index: Int,
    text: String,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Row(modifier = modifier) {
        Text(
            text = index.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(spacing.medium))
        Text(
            text = text,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
