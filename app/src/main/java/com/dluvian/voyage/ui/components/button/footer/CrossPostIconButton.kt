package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.OpenCrossPostCreation
import com.dluvian.voyage.ui.theme.CrossPostIcon
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId

@Composable
fun CrossPostIconButton(event: Event, onUpdate: (Cmd) -> Unit) {
    FooterIconButton(
        icon = CrossPostIcon,
        description = stringResource(id = R.string.cross_post),
        onClick = { onUpdate(OpenCrossPostCreation(event)) })
}
