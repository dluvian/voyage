package com.dluvian.voyage.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.SystemBackPress
import com.dluvian.voyage.ui.theme.VoyageTheme

@Composable
fun VoyageApp(core: Core) {
    BackHandler { core.onUpdate(SystemBackPress) }

    VoyageTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            VoyageAppContent(core = core)
        }
    }
}
