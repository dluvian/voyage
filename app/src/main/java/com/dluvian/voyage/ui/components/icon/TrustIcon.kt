package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.ui.theme.FriendIcon
import com.dluvian.voyage.ui.theme.TrustedIcon
import com.dluvian.voyage.ui.theme.UnknownIcon
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.sizing


// TODO: Info box when clicked to explain 2 different icons
@Composable
fun TrustIcon(trustType: TrustType) {
    val (icon, color, description) = when (trustType) {
        FriendTrust -> Triple(
            FriendIcon,
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.friend)
        )

        WebTrust -> Triple(
            TrustedIcon,
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.trusted)
        )

        NoTrust -> Triple(
            UnknownIcon,
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.unknown)
        )
    }
    Icon(
        modifier = Modifier.size(sizing.smallIndicator),
        imageVector = icon,
        contentDescription = description,
        tint = color
    )
}
