package com.dluvian.voyage.ui.components.row.uiEvent

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.MAX_LINES_CONTENT
import com.dluvian.voyage.MAX_LINES_SUBJECT
import com.dluvian.voyage.isReply
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FeedCtx
import com.dluvian.voyage.model.OpenThread
import com.dluvian.voyage.model.ThreadReplyCtx
import com.dluvian.voyage.model.ThreadRootCtx
import com.dluvian.voyage.model.ThreadViewShowReplies
import com.dluvian.voyage.model.ThreadViewToggleCollapse
import com.dluvian.voyage.model.UICtx
import com.dluvian.voyage.subject
import com.dluvian.voyage.ui.components.button.footer.CountedReplyButton
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.nonMain.MoreRepliesTextButton

@Composable
fun UIEventRow(
    ctx: UICtx,
    onUpdate: (Cmd) -> Unit
) {
    when (ctx) {
        is FeedCtx -> UIEventMainRow(
            ctx = ctx,
            onUpdate = onUpdate
        )

        is ThreadRootCtx -> {
            when (ctx.uiEvent.event.isReply()) {
                true -> RowWithDivider(level = 1) {
                    UIEventMainRow(
                        ctx = ctx,
                        onUpdate = onUpdate
                    )
                }

                false -> UIEventMainRow(
                    ctx = ctx,
                    onUpdate = onUpdate
                )
            }
        }

        is ThreadReplyCtx -> {
            RowWithDivider(level = ctx.level) {
                UIEventMainRow(
                    ctx = ctx,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
private fun UIEventMainRow(
    ctx: UICtx,
    onUpdate: (Cmd) -> Unit
) {
    val onClickRow = {
        when (ctx) {
            is ThreadReplyCtx -> onUpdate(ThreadViewToggleCollapse(id = ctx.uiEvent.event.id()))
            is FeedCtx -> {
                val event = ctx.uiEvent.inner?.event ?: ctx.uiEvent.event
                onUpdate(OpenThread(event))
            }

            is ThreadRootCtx -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickRow)
            .padding(vertical = spacing.bigScreenEdge)
            .padding(start = spacing.bigScreenEdge)
    ) {
        MainEventHeader(
            ctx = ctx,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))

        // Another col for end padding excluding header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = spacing.bigScreenEdge)
        ) {
            ctx.uiEvent.event.subject()?.let { subject ->
                if (subject.isNotEmpty()) {
                    AnnotatedText(
                        text = AnnotatedString(subject),
                        maxLines = MAX_LINES_SUBJECT,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(spacing.large))
                }
            }

            val isCollapsed = remember(ctx) {
                when (ctx) {
                    is FeedCtx, is ThreadRootCtx -> true
                    is ThreadReplyCtx -> ctx.isCollapsed
                }
            }
            AnimatedVisibility(
                visible = !isCollapsed,
                exit = slideOutVertically(animationSpec = tween(durationMillis = 0))
            ) {
                AnnotatedText(
                    text = ctx.uiEvent.annotatedContent,
                    maxLines = when (ctx) {
                        is ThreadReplyCtx, is ThreadRootCtx -> Int.MAX_VALUE
                        is FeedCtx -> MAX_LINES_CONTENT
                    }
                )
                Spacer(modifier = Modifier.height(spacing.large))
            }

            if (!isCollapsed) UIEventActions(
                uiEvent = ctx.uiEvent,
                onUpdate = onUpdate,
                additionalStartAction = {
                    when (ctx) {
                        is ThreadReplyCtx -> {
                            if (ctx.hasReplies) {
                                MoreRepliesTextButton(
                                    onShowReplies = {
                                        onUpdate(ThreadViewShowReplies(ctx.uiEvent.event.id()))
                                    }
                                )
                            }
                        }

                        is FeedCtx, is ThreadRootCtx -> {}
                    }

                },
                additionalEndAction = {
                    CountedReplyButton(ctx = ctx, onUpdate = onUpdate)
                })
        }
    }
}

@Composable
private fun RowWithDivider(level: Int, content: @Composable () -> Unit) {
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
