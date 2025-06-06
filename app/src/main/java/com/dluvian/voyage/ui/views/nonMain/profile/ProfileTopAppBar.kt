package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.ClickEditProfile
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.OneselfProfile
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.TrustedProfile
import com.dluvian.voyage.model.UnfollowProfile
import com.dluvian.voyage.model.UnknownProfile
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton


@Composable
fun ProfileTopAppBar(
    profile: TrustProfile,
    onUpdate: (Cmd) -> Unit
) {
    SimpleGoBackTopAppBar(
        title = profile.uiName(),
        actions = {
            when (profile) {
                is FollowedProfile, is TrustedProfile, is UnknownProfile -> {
                    FollowButton(
                        isFollowed = profile is FollowedProfile,
                        onFollow = {
                            onUpdate(FollowProfile(profile.pubkey))
                        },
                        onUnfollow = {
                            onUpdate(UnfollowProfile(profile.pubkey))
                        })
                }

                is OneselfProfile -> {
                    Button(onClick = { onUpdate(ClickEditProfile) }) {
                        Text(text = stringResource(id = R.string.edit))
                    }
                }
            }
        },
        onUpdate = onUpdate
    )
}
