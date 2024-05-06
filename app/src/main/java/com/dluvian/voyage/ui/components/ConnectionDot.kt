package com.dluvian.voyage.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.dluvian.voyage.core.model.ConnectionStatus
import com.dluvian.voyage.core.model.Waiting
import com.dluvian.voyage.ui.theme.sizing

@Composable
fun ConnectionDot(connectionStatus: ConnectionStatus) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val infiniteTransition = rememberInfiniteTransition(label = "Connection dot")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = if (connectionStatus is Waiting) backgroundColor
        else connectionStatus.getColor(),
        targetValue = connectionStatus.getColor(),
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "connection color"
    )
    Box(
        modifier = Modifier
            .size(sizing.dot)
            .clip(CircleShape)
            .background(color = animatedColor)
    )
}
