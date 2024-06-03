package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn

@Composable
fun FollowButton(isFollowed: Boolean, onFollow: Fn, onUnfollow: Fn) {
    Button(
        modifier = Modifier.height(ButtonDefaults.MinHeight),
        onClick = { if (isFollowed) onUnfollow() else onFollow() },
        colors = if (isFollowed) ButtonDefaults.buttonColors().copy(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
        else ButtonDefaults.buttonColors(),
        border = if (isFollowed) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onBackground
        ) else null
    ) {
        Text(
            text = if (isFollowed) stringResource(id = R.string.followed)
            else stringResource(id = R.string.follow)
        )
    }
}
