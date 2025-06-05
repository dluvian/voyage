package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.button.ProfileOrTopicOptionButton


@Composable
fun ProfileTopAppBar(
    profile: TrustProfile,
    onUpdate: (Cmd) -> Unit
) {
    SimpleGoBackTopAppBar(
        title = profile.inner.name,
        actions = {
            if (!profile.inner.isMe) {
                ProfileOrTopicOptionButton(
                    item = ItemSetProfile(pubkey = profile.inner.pubkey),
                    addableLists = addableLists,
                    nonAddableLists = nonAddableLists,
                    scope = rememberCoroutineScope(),
                    onUpdate = onUpdate
                )
                FollowButton(
                    isFollowed = profile.inner.isFriend,
                    onFollow = {
                        onUpdate(FollowProfile(pubkey = profile.inner.pubkey))
                    },
                    onUnfollow = {
                        onUpdate(UnfollowProfile(pubkey = profile.inner.pubkey))
                    })
            } else {
                Button(onClick = { onUpdate(ClickEditProfile) }) {
                    Text(text = stringResource(id = R.string.edit))
                }
            }
        },
        onUpdate = onUpdate
    )
}
