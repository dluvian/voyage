package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewFollowProfile
import com.dluvian.voyage.core.ProfileViewUnfollowProfile
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton


@Composable
fun ProfileTopAppBar(name: String, pubkey: PubkeyHex, isFollowed: Boolean, onUpdate: OnUpdate) {
    GoBackTopAppBar(
        title = {
            Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = {
            FollowButton(
                isFollowed = isFollowed,
                onFollow = { onUpdate(ProfileViewFollowProfile(pubkey)) },
                onUnfollow = { onUpdate(ProfileViewUnfollowProfile(pubkey)) })
        },
        onUpdate = onUpdate
    )
}
