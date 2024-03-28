package com.dluvian.voyage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nostr_kt.createNevent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenThread
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun PostRow(post: RootPostUI, isDetailed: Boolean = false, onUpdate: OnUpdate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onUpdate(OpenThread(nevent = createNevent(hex = post.id))) })
            .padding(spacing.screenEdge)
    ) {
        PostRowHeader(
            trustType = post.trustType,
            authorName = post.authorName,
            pubkey = post.pubkey,
            isDetailed = isDetailed,
            createdAt = post.createdAt,
            myTopic = post.myTopic,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))
        if (post.title.isNotEmpty()) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (isDetailed) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }
        Text(
            text = post.content,
            maxLines = if (isDetailed) Int.MAX_VALUE else 12,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(spacing.large))
        PostRowActions(
            id = post.id,
            pubkey = post.pubkey,
            myVote = post.myVote,
            tally = post.tally,
            onUpdate = onUpdate,
            additionalAction = {
                CommentChip(
                    commentCount = post.commentCount,
                    onClick = { onUpdate(OpenThread(nevent = createNevent(hex = post.id))) })
            })
    }
}
