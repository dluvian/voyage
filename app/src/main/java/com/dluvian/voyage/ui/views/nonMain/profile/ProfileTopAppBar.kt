package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickEditProfile
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton


@Composable
fun ProfileTopAppBar(profile: FullProfileUI, onUpdate: OnUpdate) {
    SimpleGoBackTopAppBar(
        title = profile.inner.name,
        actions = {
            if (!profile.inner.isMe) FollowButton(
                isFollowed = profile.inner.isFriend,
                onFollow = {
                    onUpdate(FollowProfile(pubkey = profile.inner.pubkey))
                },
                onUnfollow = {
                    onUpdate(UnfollowProfile(pubkey = profile.inner.pubkey))
                })
            else Button(onClick = { onUpdate(ClickEditProfile) }) {
                Text(text = stringResource(id = R.string.edit))
            }
        },
        onUpdate = onUpdate
    )
}
