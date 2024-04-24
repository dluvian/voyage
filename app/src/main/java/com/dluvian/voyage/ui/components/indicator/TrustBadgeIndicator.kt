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
fun TrustBadgeIndicator(trustType: TrustType, isSmall: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        TrustIcon(
            modifier = Modifier
                .fillMaxSize(if (isSmall) 0.4f else 0.5f)
                .aspectRatio(1.0f),
            trustType = trustType
        )
    }
}
