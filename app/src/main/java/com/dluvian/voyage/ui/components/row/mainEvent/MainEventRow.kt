package com.dluvian.voyage.ui.components.row.mainEvent

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
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.components.row.mainEvent.old.FeedItemActions
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun MainEventRow(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    val onOpenThread = { onUpdate(OpenThread(mainEvent = ctx.mainEvent)) }
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
            ctx = ctx,
            showAuthorName = showAuthorName,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))

        ctx.mainEvent.getSubject()?.let { subject ->
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
            text = ctx.mainEvent.content,
            onClick = { onClickText(it, ctx.mainEvent.content) }
        )
        Spacer(modifier = Modifier.height(spacing.large))
        FeedItemActions(
            mainEvent = ctx.mainEvent,
            onUpdate = onUpdate,
            additionalEndAction = {
                CommentChip(
                    commentCount = ctx.mainEvent.replyCount,
                    onClick = { onUpdate(OpenReplyCreation(parent = ctx.mainEvent)) })
            })
    }
}