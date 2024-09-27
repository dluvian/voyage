package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString

@Composable
fun AnnotatedTextWithHeader(
    modifier: Modifier = Modifier,
    header: String,
    text: AnnotatedString,
) {
    Column(modifier = modifier) {
        SmallHeader(header = header)
        AnnotatedText(text = text)
    }
}
