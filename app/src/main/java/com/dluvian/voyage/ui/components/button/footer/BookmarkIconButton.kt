package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.UnbookmarkPost
import com.dluvian.voyage.ui.theme.BookmarkIcon
import rust.nostr.sdk.EventId

@Composable
fun BookmarkIconButton(id: EventId, onUpdate: (Cmd) -> Unit) {
    FooterIconButton(
        icon = BookmarkIcon,
        description = stringResource(id = R.string.remove_bookmark),
        onClick = { onUpdate(UnbookmarkPost(id)) })
}
