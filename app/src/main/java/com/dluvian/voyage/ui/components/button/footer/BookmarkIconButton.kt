package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.UnbookmarkPost
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.theme.BookmarkIcon

@Composable
fun BookmarkIconButton(relevantId: EventIdHex, onUpdate: OnUpdate) {
    FooterIconButton(
        icon = BookmarkIcon,
        description = stringResource(id = R.string.remove_bookmark),
        onClick = { onUpdate(UnbookmarkPost(postId = relevantId)) })
}
