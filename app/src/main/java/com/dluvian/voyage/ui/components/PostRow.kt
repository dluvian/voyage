package com.dluvian.voyage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenReplyCreation
import com.dluvian.voyage.core.OpenThread
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun PostRow(post: RootPostUI, isDetailed: Boolean = false, onUpdate: OnUpdate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onUpdate(OpenThread(rootPost = post)) })
            .padding(spacing.screenEdge)
    ) {
        val uriHandler = LocalUriHandler.current
        val onClickText = { offset: Int, text: AnnotatedString ->
            onUpdate(
                ClickText(
                    text = text,
                    offset = offset,
                    uriHandler = uriHandler,
                    rootPost = post
                )
            )
        }
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
            AnnotatedText(
                text = post.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = if (isDetailed) Int.MAX_VALUE else 3,
                onClick = { onClickText(it, post.title) }
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }
        AnnotatedText(
            text = post.content,
            maxLines = if (isDetailed) Int.MAX_VALUE else 12,
            onClick = { onClickText(it, post.content) }
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
                    commentCount = post.replyCount,
                    onClick = { onUpdate(OpenReplyCreation(parent = post)) })
            })
    }
}
