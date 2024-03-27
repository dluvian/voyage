package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.interactor.Vote

@Composable
fun PostRowActions(
    id: EventIdHex,
    pubkey: PubkeyHex,
    myVote: Vote,
    tally: Int,
    onUpdate: OnUpdate,
    additionalAction: ComposableContent = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VoteBox(
            postId = id,
            authorPubkey = pubkey,
            myVote = myVote,
            tally = tally,
            onUpdate = onUpdate
        )
        additionalAction()
    }
}
