package com.dluvian.voyage.ui.components.chip

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickNeutralizeVote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.ui.theme.UpvoteColor
import com.dluvian.voyage.ui.theme.UpvoteIcon
import com.dluvian.voyage.ui.theme.UpvoteOffIcon

@Composable
fun UpvoteChip(
    upvoteCount: Int,
    isUpvoted: Boolean,
    postId: EventIdHex,
    authorPubkey: PubkeyHex,
    onUpdate: OnUpdate
) {
    ActionChip(
        icon = if (isUpvoted) UpvoteIcon else UpvoteOffIcon,
        count = upvoteCount,
        description = if (isUpvoted) {
            stringResource(id = R.string.remove_upvote)
        } else {
            stringResource(id = R.string.upvote)
        },
        tint = if (isUpvoted) UpvoteColor else MaterialTheme.colorScheme.primary,
        onClick = {
            if (isUpvoted) {
                onUpdate(ClickNeutralizeVote(postId = postId, mention = authorPubkey))
            } else {
                onUpdate(ClickUpvote(postId = postId, mention = authorPubkey))
            }
        }
    )
}
