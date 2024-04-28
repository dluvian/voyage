package com.dluvian.voyage.ui.components.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.getTrustIcon


@Composable
fun TrustIcon(modifier: Modifier = Modifier, trustType: TrustType) {
    val (icon, color, description) = when (trustType) {
        Oneself -> Triple(
            getTrustIcon(trustType = trustType),
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.oneself)
        )

        FriendTrust -> Triple(
            getTrustIcon(trustType = trustType),
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.friend)
        )

        WebTrust -> Triple(
            getTrustIcon(trustType = trustType),
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.trusted)
        )

        NoTrust -> Triple(
            getTrustIcon(trustType = trustType),
            getTrustColor(trustType = trustType),
            stringResource(id = R.string.unknown)
        )
    }
    Icon(
        modifier = modifier,
        imageVector = icon,
        contentDescription = description,
        tint = color
    )
}
