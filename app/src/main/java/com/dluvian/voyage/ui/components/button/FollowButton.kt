package com.dluvian.voyage.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn

@Composable
fun FollowButton(isFollowed: Boolean, onFollow: Fn, onUnfollow: Fn) {
    ActionButton(
        isActive = isFollowed,
        activeLabel = stringResource(id = R.string.followed),
        unactiveLabel = stringResource(id = R.string.follow),
        onActivate = onFollow,
        onDeactivate = onUnfollow
    )
}
