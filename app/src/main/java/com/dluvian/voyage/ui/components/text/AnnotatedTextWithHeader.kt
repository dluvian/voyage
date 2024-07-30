package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.OnUpdate

@Composable
fun AnnotatedTextWithHeader(
    modifier: Modifier = Modifier,
    header: String,
    text: AnnotatedString,
    onUpdate: OnUpdate
) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = modifier) {
        SmallHeader(header = header)
        AnnotatedText(
            text = text,
            onClick = { offset ->
                onUpdate(ClickText(text = text, offset = offset, uriHandler = uriHandler))
            }
        )
    }
}
