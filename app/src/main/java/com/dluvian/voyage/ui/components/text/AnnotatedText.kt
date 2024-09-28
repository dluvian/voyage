package com.dluvian.voyage.ui.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AnnotatedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
) {
    val color = MaterialTheme.colorScheme.onSurface
    val annotatedString = remember(text, style) {
        useDefaultTextStyle(text = text, style = style.copy(color = color))
    }
    Text(
        modifier = modifier,
        text = annotatedString,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun useDefaultTextStyle(text: AnnotatedString, style: TextStyle): AnnotatedString {
    return buildAnnotatedString {
        val index = pushStyle(style.toSpanStyle())
        append(text)
        pop(index)
    }
}
