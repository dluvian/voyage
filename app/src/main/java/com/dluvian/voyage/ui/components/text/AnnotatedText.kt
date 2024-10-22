package com.dluvian.voyage.ui.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AnnotatedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}
