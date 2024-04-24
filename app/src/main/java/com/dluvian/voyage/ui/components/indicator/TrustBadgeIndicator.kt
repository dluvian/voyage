package com.dluvian.voyage.ui.components.indicator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.icon.TrustIcon

@Composable
fun TrustBadgeIndicator(trustType: TrustType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        TrustIcon(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .aspectRatio(1.0f),
            trustType = trustType
        )
    }
}
