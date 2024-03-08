package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun EdgeToEdgeColWithDivider(verticalPadding: Dp = 0.dp, content: ComposableContent) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screenEdge, vertical = verticalPadding)
        ) {
            content()
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = spacing.tiny)
    }
}
