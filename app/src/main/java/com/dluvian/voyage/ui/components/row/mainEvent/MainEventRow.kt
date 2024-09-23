package com.dluvian.voyage.ui.components.row.mainEvent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenReplyCreation
import com.dluvian.voyage.core.OpenThread
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.ReplyIcon
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.nonMain.MoreRepliesTextButton

@Composable
fun MainEventRow(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    when (ctx) {
        is FeedCtx, is ThreadRootCtx -> MainEventRowContent(
            ctx = ctx,
            showAuthorName = showAuthorName,
            onUpdate = onUpdate
        )

        is ThreadReplyCtx -> {
            RowWithDivider(level = ctx.level) {
                MainEventRowContent(
                    ctx = ctx,
                    showAuthorName = showAuthorName,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
private fun MainEventRowContent(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    val onClickRow = {
        when (ctx) {
            is ThreadReplyCtx -> onUpdate(ThreadViewToggleCollapse(id = ctx.reply.id))
            is FeedCtx -> onUpdate(OpenThread(mainEvent = ctx.mainEvent))
            is ThreadRootCtx -> {}
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickRow)
            .padding(spacing.screenEdge)
    ) {
        val uriHandler = LocalUriHandler.current
        val onClickText = { offset: Int, text: AnnotatedString ->
            onUpdate(
                ClickText(
                    text = text,
                    offset = offset,
                    uriHandler = uriHandler,
                    onNoneClick = onClickRow
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
            additionalStartAction = {
                when (ctx) {
                    is ThreadReplyCtx -> {
                        if (ctx.reply.replyCount > 0 && !ctx.hasLoadedReplies) {
                            MoreRepliesTextButton(
                                replyCount = ctx.reply.replyCount,
                                onShowReplies = {
                                    onUpdate(ThreadViewShowReplies(id = ctx.reply.id))
                                }
                            )
                        }
                    }

                    is FeedCtx, is ThreadRootCtx -> {}
                }

            },
            additionalEndAction = {
                when (ctx) {
                    is ThreadReplyCtx -> TextButton(
                        modifier = Modifier.height(ButtonDefaults.MinHeight),
                        onClick = { onUpdate(OpenReplyCreation(parent = ctx.reply)) }
                    ) {
                        Icon(
                            imageVector = ReplyIcon,
                            contentDescription = stringResource(id = R.string.reply)
                        )
                    }

                    is FeedCtx, is ThreadRootCtx -> CommentChip(
                        commentCount = ctx.mainEvent.replyCount,
                        onClick = { onUpdate(OpenReplyCreation(parent = ctx.mainEvent)) })
                }

            })
    }
}

@Composable
private fun RowWithDivider(level: Int, content: ComposableContent) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        repeat(times = level) {
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = spacing.large, end = spacing.medium)
            )
        }
        content()
    }
}
