package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IsInListTrust
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.getTrustIcon
import com.dluvian.voyage.ui.theme.sizing


@Composable
fun TrustIcon(modifier: Modifier = Modifier, trustType: TrustType) {
    val description = when (trustType) {
        Oneself -> stringResource(id = R.string.oneself)
        FriendTrust -> stringResource(id = R.string.friend)
        IsInListTrust -> stringResource(id = R.string.in_list)
        WebTrust -> stringResource(id = R.string.trusted)
        Muted -> stringResource(id = R.string.muted)
        NoTrust -> stringResource(id = R.string.unknown)
    }

    Icon(
        modifier = modifier.size(sizing.smallIndicator),
        imageVector = getTrustIcon(trustType = trustType),
        contentDescription = description,
        tint = getTrustColor(trustType = trustType)
    )
}

@Composable
fun TrustIcon(modifier: Modifier = Modifier, profile: AdvancedProfileView) {
    TrustIcon(
        modifier = modifier,
        trustType = TrustType.from(
            isOneself = profile.isMe,
            isFriend = profile.isFriend,
            isWebOfTrust = profile.isWebOfTrust,
            isMuted = profile.isMuted,
            isInList = profile.isInList
        )
    )
}
