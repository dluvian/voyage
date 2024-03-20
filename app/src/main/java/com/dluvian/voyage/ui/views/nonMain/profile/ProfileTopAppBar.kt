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
                text = profile.advancedProfile.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            FollowButton(
                isFollowed = profile.advancedProfile.isFriend,
                onFollow = { onUpdate(ProfileViewFollowProfile(profile.advancedProfile.pubkey)) },
                onUnfollow = { onUpdate(ProfileViewUnfollowProfile(profile.advancedProfile.pubkey)) })
        },
        onUpdate = onUpdate
    )
}
