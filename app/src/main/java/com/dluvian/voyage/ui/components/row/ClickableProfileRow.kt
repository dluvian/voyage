package com.dluvian.voyage.ui.components.row

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.icon.TrustIcon


@Composable
fun ClickableProfileRow(profile: AdvancedProfileView, onClick: Fn) {
    ClickableRow(
        header = profile.name,
        leadingContent = {
            TrustIcon(
                trustType = TrustType.from(
                    isOneself = profile.isMe,
                    isFriend = profile.isFriend,
                    isWebOfTrust = profile.isWebOfTrust
                ),
            )
        },
        onClick = onClick
    )
}
