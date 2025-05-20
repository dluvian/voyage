package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IsInListTrust
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.sizing

private const val X_RATIO = 0.45f

@Stable
@Composable
fun TrustIcon(trustType: TrustType, size: Dp = sizing.trustIndicator) {
    val color = getTrustColor(trustType = trustType)
    when (trustType) {
        FriendTrust,
        WebTrust,
        NoTrust,
        Muted,
        -> TrustBox(size = size, color = color)

        IsInListTrust -> ListTrustBox(size = size, color = color)

        Oneself -> {
            /* Nothing for oneself */
        }
    }
}

@Stable
@Composable
fun TrustIcon(profile: AdvancedProfileView) {
    TrustIcon(
        trustType = TrustType.from(
            isOneself = profile.isMe,
            isFriend = profile.isFriend,
            isWebOfTrust = profile.isWebOfTrust,
            isMuted = profile.isMuted,
            isInList = profile.isInList,
        )
    )
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

@Stable
@Composable
private fun ListTrustBox(size: Dp, color: Color) {
    Column(
        modifier = Modifier
            .heightIn(
                min = size,
                max = size
            )
            .width(width = size.times(X_RATIO)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = size.div(5))
                    .background(color = color)
            )
        }
    }
}
