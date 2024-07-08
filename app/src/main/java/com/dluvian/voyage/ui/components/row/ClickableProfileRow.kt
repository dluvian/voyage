package com.dluvian.voyage.ui.components.row

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.room.view.AdvancedProfileView


@Composable
fun ClickableProfileRow(
    profile: AdvancedProfileView,
    trailingContent: ComposableContent = {},
    onClick: Fn
) {
    ClickableTrustIconRow(
        trustType = TrustType.from(
            isOneself = profile.isMe,
            isFriend = profile.isFriend,
            isWebOfTrust = profile.isWebOfTrust,
            isMuted = profile.isMuted
        ),
        header = profile.name,
        trailingContent = trailingContent,
        onClick = onClick,
        onTrustIconClick = onClick
    )
}
