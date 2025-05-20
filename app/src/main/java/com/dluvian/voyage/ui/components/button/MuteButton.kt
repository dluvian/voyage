package com.dluvian.voyage.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn

@Composable
fun MuteButton(isMuted: Boolean, onMute: Fn, onUnmute: Fn) {
    ActionButton(
        isActive = isMuted,
        activeLabel = stringResource(id = R.string.muted),
        unactiveLabel = stringResource(id = R.string.mute),
        onActivate = onMute,
        onDeactivate = onUnmute
    )
}
