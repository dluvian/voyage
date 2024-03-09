package com.dluvian.voyage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickDownvote
import com.dluvian.voyage.core.ClickNeutralizeVote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Lambda
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.Downvote
import com.dluvian.voyage.core.model.Upvote
import com.dluvian.voyage.core.model.Vote
import com.dluvian.voyage.ui.theme.DenimBlue
import com.dluvian.voyage.ui.theme.DownvoteIcon
import com.dluvian.voyage.ui.theme.LeftRoundedChip
import com.dluvian.voyage.ui.theme.RightRoundedChip
import com.dluvian.voyage.ui.theme.TallPoppyRed
import com.dluvian.voyage.ui.theme.UpvoteIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun VoteBox(
    postId: EventIdHex,
    authorPubkey: PubkeyHex,
    myVote: Vote,
    tally: Int,
    ratioInPercent: Int,
    onUpdate: OnUpdate
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        VoteButton(
            isUpvote = true,
            tint = if (myVote is Upvote) TallPoppyRed else MaterialTheme.colorScheme.onSurfaceVariant,
            description = stringResource(id = R.string.upvote),
            onClick = {
                if (myVote is Upvote)
                    onUpdate(ClickNeutralizeVote(postId = postId, pubkey = authorPubkey))
                else
                    onUpdate(ClickUpvote(postId = postId, pubkey = authorPubkey))
            }
        )
        Text(
            modifier = Modifier.padding(horizontal = spacing.small),
            text = "$tally ($ratioInPercent%)",
            fontWeight = if (myVote.isNeutral()) FontWeight.Normal else FontWeight.SemiBold
        )
        VoteButton(
            isUpvote = false,
            tint = if (myVote is Downvote) DenimBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            description = stringResource(id = R.string.downvote),
            onClick = {
                if (myVote is Downvote)
                    onUpdate(ClickNeutralizeVote(postId = postId, pubkey = authorPubkey))
                else
                    onUpdate(ClickDownvote(postId = postId, pubkey = authorPubkey))
            })
    }
}

@Composable
private fun VoteButton(
    isUpvote: Boolean,
    tint: Color,
    description: String,
    onClick: Lambda
) {
    Icon(
        modifier = Modifier
            .clip(if (isUpvote) LeftRoundedChip else RightRoundedChip)
            .clickable(onClick = onClick),
        imageVector = if (isUpvote) UpvoteIcon else DownvoteIcon,
        contentDescription = description,
        tint = tint
    )
}
