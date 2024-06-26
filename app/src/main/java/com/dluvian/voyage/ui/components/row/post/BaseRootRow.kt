package com.dluvian.voyage.ui.components.row.post

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
import com.dluvian.nostr_kt.createNevent
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenReplyCreation
import com.dluvian.voyage.core.OpenThread
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun BaseRootRow(
    post: RootPostUI,
    isOp: Boolean,
    isThread: Boolean,
    onUpdate: OnUpdate
) {
    val onOpenThread = {
        val action = if (post.crossPostedId != null) {
            OpenThreadRaw(nevent = createNevent(hex = post.crossPostedId))
        } else {
            OpenThread(rootPost = post)
        }
        onUpdate(action)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenThread)
            .padding(spacing.screenEdge)
    ) {
        val uriHandler = LocalUriHandler.current
        val onClickText = { offset: Int, text: AnnotatedString ->
            onUpdate(
                ClickText(
                    text = text,
                    offset = offset,
                    uriHandler = uriHandler,
                    onNoneClick = onOpenThread
                )
            )
        }
        ParentRowHeader(
            parent = post,
            myTopic = post.myTopic,
            isOp = isOp,
            isThreadView = isThread,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))
        if (post.subject.isNotEmpty()) {
            AnnotatedText(
                text = post.subject,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = if (isThread) Int.MAX_VALUE else 3,
                onClick = { onClickText(it, post.subject) }
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }
        AnnotatedText(
            text = post.content,
            maxLines = if (isThread) Int.MAX_VALUE else 12,
            onClick = { onClickText(it, post.content) }
        )
        Spacer(modifier = Modifier.height(spacing.large))
        PostRowActions(
            postId = post.getRelevantId(),
            authorPubkey = post.getRelevantPubkey(),
            isUpvoted = post.isUpvoted,
            upvoteCount = post.upvoteCount,
            isBookmarked = post.isBookmarked,
            onUpdate = onUpdate,
            additionalEndAction = {
                CommentChip(
                    commentCount = post.replyCount,
                    onClick = { onUpdate(OpenReplyCreation(parent = post)) })
            })
    }
}