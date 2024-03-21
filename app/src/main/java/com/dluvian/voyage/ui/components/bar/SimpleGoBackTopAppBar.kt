package com.dluvian.voyage.ui.components.bar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.OnUpdate

@Composable
fun SimpleGoBackTopAppBar(title: String, onUpdate: OnUpdate) {
    GoBackTopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        onUpdate = onUpdate
    )
}
