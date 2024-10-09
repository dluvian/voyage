package com.dluvian.voyage.ui.components.row.mainEvent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.MAX_CONTENT_LINES
import com.dluvian.voyage.core.MAX_SUBJECT_LINES
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenThread
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.ThreadableMainEvent
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.ui.components.button.OptionsButton
import com.dluvian.voyage.ui.components.button.footer.CountedCommentButton
import com.dluvian.voyage.ui.components.button.footer.ReplyIconButton
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.nonMain.MoreRepliesTextButton

@Composable
fun MainEventRow(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    when (ctx) {
        is FeedCtx -> MainEventMainRow(
            ctx = ctx,
            showAuthorName = showAuthorName,
            onUpdate = onUpdate
        )

        is ThreadRootCtx -> {
            when (ctx.threadableMainEvent) {
                is RootPost -> MainEventMainRow(
                    ctx = ctx,
                    showAuthorName = showAuthorName,
                    onUpdate = onUpdate
                )

                is LegacyReply, is Comment -> RowWithDivider(level = 1) {
                    MainEventMainRow(
                        ctx = ctx,
                        showAuthorName = showAuthorName,
                        onUpdate = onUpdate
                    )
                }
            }
        }

        is ThreadReplyCtx -> {
            RowWithDivider(level = ctx.level) {
                MainEventMainRow(
                    ctx = ctx,
                    showAuthorName = showAuthorName,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
private fun MainEventMainRow(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    val onClickRow = {
        when (ctx) {
            is ThreadReplyCtx -> onUpdate(ThreadViewToggleCollapse(id = ctx.reply.id))
            is FeedCtx -> {
                when (val event = ctx.mainEvent) {
                    is ThreadableMainEvent -> onUpdate(OpenThread(mainEvent = event))
                    is CrossPost -> onUpdate(OpenThreadRaw(nevent = createNevent(hex = event.crossPostedId)))
                }
            }

            is ThreadRootCtx -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickRow)
            .padding(spacing.bigScreenEdge)
    ) {
        MainEventHeader(
            ctx = ctx,
            showAuthorName = showAuthorName,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))

        ctx.mainEvent.getRelevantSubject()?.let { subject ->
            if (subject.isNotEmpty()) {
                AnnotatedText(
                    text = subject,
                    maxLines = MAX_SUBJECT_LINES,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(spacing.large))
            }
        }

        AnimatedVisibility(
            visible = !ctx.isCollapsedReply(),
            exit = slideOutVertically(animationSpec = tween(durationMillis = 0))
        ) {
            AnnotatedText(
                text = ctx.mainEvent.content,
                maxLines = when (ctx) {
                    is ThreadReplyCtx, is ThreadRootCtx -> Int.MAX_VALUE
                    is FeedCtx -> MAX_CONTENT_LINES
                }
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }

        if (!ctx.isCollapsedReply()) MainEventActions(
            mainEvent = ctx.mainEvent,
            onUpdate = onUpdate,
            additionalStartAction = {
                OptionsButton(mainEvent = ctx.mainEvent, onUpdate = onUpdate)
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
                    is ThreadReplyCtx -> ReplyIconButton(ctx = ctx, onUpdate = onUpdate)

                    is ThreadRootCtx -> CountedCommentButton(ctx = ctx, onUpdate = onUpdate)

                    is FeedCtx -> {
                        when (ctx.mainEvent) {
                            is RootPost,
                            is CrossPost -> CountedCommentButton(ctx = ctx, onUpdate = onUpdate)

                            is LegacyReply, is Comment -> ReplyIconButton(
                                ctx = ctx,
                                onUpdate = onUpdate
                            )
                        }
                    }
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
