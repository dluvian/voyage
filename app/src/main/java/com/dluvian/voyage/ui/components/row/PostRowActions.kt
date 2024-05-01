package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenCrossPostCreation
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.ui.components.VoteBox
import com.dluvian.voyage.ui.components.chip.CrossPostChip
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun PostRowActions(
    id: EventIdHex,
    pubkey: PubkeyHex,
    myVote: Vote,
    upvoteCount: Int,
    downvoteCount: Int,
    onUpdate: OnUpdate,
    additionalStartAction: ComposableContent = {},
    additionalEndAction: ComposableContent = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        additionalStartAction()
        Spacer(modifier = Modifier.width(spacing.tiny))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CrossPostChip(onClick = { onUpdate(OpenCrossPostCreation(id = id)) })
            additionalEndAction()
            VoteBox(
                postId = id,
                authorPubkey = pubkey,
                myVote = myVote,
                upvoteCount = upvoteCount,
                downvoteCount = downvoteCount,
                onUpdate = onUpdate
            )
        }
    }
}
