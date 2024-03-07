package com.dluvian.voyage.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.ui.theme.VoyageTheme

@Composable
fun VoyageApp(core: Core){
    VoyageTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            VoyageNavigator(core = core)
        }
    }
}
