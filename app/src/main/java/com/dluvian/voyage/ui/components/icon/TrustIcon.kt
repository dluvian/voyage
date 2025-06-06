package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.OneselfProfile
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.TrustedProfile
import com.dluvian.voyage.model.UnknownProfile
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.sizing

private const val X_RATIO = 0.45f

@Stable
@Composable
fun TrustIcon(profile: TrustProfile, size: Dp = sizing.trustIndicator) {
    val color = getTrustColor(profile)
    when (profile) {
        is FollowedProfile,
        is TrustedProfile,
        is UnknownProfile -> TrustBox(size = size, color = color)

        is OneselfProfile -> {
            /* Nothing for oneself */
        }
    }
}

@Stable
@Composable
private fun TrustBox(size: Dp, color: Color) {
    Box(
        modifier = Modifier
            .height(height = size)
            .width(width = size.times(X_RATIO))
            .background(color = color)
    )
}
