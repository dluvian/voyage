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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.dluvian.voyage.ui.theme.sizing
import rust.nostr.sdk.RelayStatus

@Composable
fun ConnectionDot(status: RelayStatus) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val infiniteTransition = rememberInfiniteTransition(label = "Connection dot")
    val color = remember(status) {
        when (status) {
            RelayStatus.INITIALIZED, RelayStatus.PENDING, RelayStatus.CONNECTING -> Color.Yellow
            RelayStatus.CONNECTED -> Color.Green
            RelayStatus.DISCONNECTED -> Color.LightGray
            RelayStatus.TERMINATED, RelayStatus.BANNED -> Color.Red
        }
    }
    val isBlinking = remember(status) {
        when (status) {
            RelayStatus.INITIALIZED, RelayStatus.PENDING, RelayStatus.CONNECTING -> true
            RelayStatus.CONNECTED, RelayStatus.DISCONNECTED, RelayStatus.TERMINATED, RelayStatus.BANNED -> false
        }
    }
    val animatedColor by infiniteTransition.animateColor(
        initialValue = if (isBlinking) backgroundColor else color,
        targetValue = color,
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

