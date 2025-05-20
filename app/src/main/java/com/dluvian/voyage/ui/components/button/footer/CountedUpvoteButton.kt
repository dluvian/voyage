package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickNeutralizeVote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.ui.theme.UpvoteIcon
import com.dluvian.voyage.ui.theme.UpvoteOffIcon

@Composable
fun CountedUpvoteButton(mainEvent: MainEvent, onUpdate: OnUpdate) {
    CountedIconButton(
        count = mainEvent.upvoteCount,
        icon = if (mainEvent.isUpvoted) UpvoteIcon else UpvoteOffIcon,
        description = if (mainEvent.isUpvoted) {
            stringResource(id = R.string.remove_upvote)
        } else {
            stringResource(id = R.string.upvote)
        },
        onClick = {
            if (mainEvent.isUpvoted) {
                onUpdate(
                    ClickNeutralizeVote(
                        postId = mainEvent.getRelevantId(),
                        mention = mainEvent.getRelevantPubkey()
                    )
                )
            } else {
                onUpdate(
                    ClickUpvote(
                        postId = mainEvent.getRelevantId(),
                        mention = mainEvent.getRelevantPubkey()
                    )
                )
            }
        },
    )
}
