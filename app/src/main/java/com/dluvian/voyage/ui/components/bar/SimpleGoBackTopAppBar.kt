package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.model.Cmd

@Composable
fun SimpleGoBackTopAppBar(
    title: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onUpdate: (Cmd) -> Unit
) {
    GoBackTopAppBar(
        title = { title?.let { Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        actions = actions,
        onUpdate = onUpdate
    )
}
