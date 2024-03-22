package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewFollowProfile
import com.dluvian.voyage.core.ProfileViewUnfollowProfile
import com.dluvian.voyage.data.model.FullProfile
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton


@Composable
fun ProfileTopAppBar(profile: FullProfile, onUpdate: OnUpdate) {
    GoBackTopAppBar(
        title = {
            Text(
                text = profile.inner.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            FollowButton(
                isFollowed = profile.inner.isFriend,
                onFollow = { onUpdate(ProfileViewFollowProfile(profile.inner.pubkey)) },
                onUnfollow = { onUpdate(ProfileViewUnfollowProfile(profile.inner.pubkey)) })
        },
        onUpdate = onUpdate
    )
}
