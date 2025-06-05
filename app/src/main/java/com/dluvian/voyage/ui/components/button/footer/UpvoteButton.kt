package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.ClickNeutralizeVotes
import com.dluvian.voyage.model.ClickUpvote
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.ui.theme.UpvoteIcon
import com.dluvian.voyage.ui.theme.UpvoteOffIcon

@Composable
fun UpvoteButton(uiEvent: UIEvent, onUpdate: (Cmd) -> Unit) {
    CountedIconButton(
        count = 0u,
        icon = if (uiEvent.upvoted) UpvoteIcon else UpvoteOffIcon,
        description = if (uiEvent.upvoted) {
            stringResource(id = R.string.remove_upvote)
        } else {
            stringResource(id = R.string.upvote)
        },
        onClick = {
            if (uiEvent.upvoted) onUpdate(ClickNeutralizeVotes(uiEvent.event))
            else onUpdate(ClickUpvote(uiEvent.event))
        },
    )
}
