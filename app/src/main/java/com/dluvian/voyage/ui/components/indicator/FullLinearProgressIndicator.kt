package com.dluvian.voyage.ui.components.indicator

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FullLinearProgressIndicator() {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}
