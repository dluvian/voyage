package com.dluvian.voyage.ui.components.row.feedItem

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
import com.dluvian.voyage.core.model.FeedItemUI
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun FeedItemRow(
    feedItem: FeedItemUI,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    val onOpenThread = { onUpdate(OpenThread(feedItem = feedItem)) }
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
        FeedItemHeader(
            feedItem = feedItem,
            showAuthorName = showAuthorName,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))

        feedItem.getSubject()?.let { subject ->
            if (subject.isNotEmpty()) {
                AnnotatedText(
                    text = subject,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    onClick = { onClickText(it, subject) }
                )
                Spacer(modifier = Modifier.height(spacing.large))
            }
        }
        AnnotatedText(
            text = feedItem.content,
            onClick = { onClickText(it, feedItem.content) }
        )
        Spacer(modifier = Modifier.height(spacing.large))
        FeedItemActions(
            feedItem = feedItem,
            onUpdate = onUpdate,
            additionalEndAction = {
                CommentChip(
                    commentCount = feedItem.replyCount,
                    onClick = { onUpdate(OpenReplyCreation(parent = feedItem)) })
            })
    }
}