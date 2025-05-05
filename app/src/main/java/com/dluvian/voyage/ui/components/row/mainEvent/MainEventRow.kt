package com.dluvian.voyage.ui.components.row.mainEvent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.MAX_CONTENT_LINES
import com.dluvian.voyage.core.MAX_SUBJECT_LINES
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenThread
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.VotePollOption
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.Poll
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.ThreadableMainEvent
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.nostr.getCurrentSecs
import com.dluvian.voyage.ui.components.button.footer.CommentButton
import com.dluvian.voyage.ui.components.button.footer.ReplyIconButton
import com.dluvian.voyage.ui.components.row.PollOptionRow
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
                is RootPost, is Poll -> MainEventMainRow(
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
            .padding(vertical = spacing.bigScreenEdge)
            .padding(start = spacing.bigScreenEdge)
    ) {
        MainEventHeader(
            ctx = ctx,
            showAuthorName = showAuthorName,
            onUpdate = onUpdate,
        )
        Spacer(modifier = Modifier.height(spacing.large))

        // Another col for end padding excluding header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = spacing.bigScreenEdge)
        ) {
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

            when (val event = ctx.mainEvent) {
                is Poll -> PollColumn(poll = event, onUpdate = onUpdate, onClickRow = onClickRow)
                is CrossPost,
                is RootPost,
                is Comment,
                is LegacyReply -> {
                }
            }

            if (!ctx.isCollapsedReply()) MainEventActions(
                mainEvent = ctx.mainEvent,
                onUpdate = onUpdate,
                additionalStartAction = {
                    when (ctx) {
                        is ThreadReplyCtx -> {
                            // TODO: Fix
                            if (true) {//ctx.reply.replyCount > 0 && !ctx.hasLoadedReplies) {
                                MoreRepliesTextButton(
                                    replyCount = 21, //ctx.reply.replyCount,
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

                        is ThreadRootCtx -> CommentButton(ctx = ctx, onUpdate = onUpdate)

                        is FeedCtx -> {
                            when (ctx.mainEvent) {
                                is RootPost,
                                is Poll,
                                is CrossPost -> CommentButton(ctx = ctx, onUpdate = onUpdate)

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

@Composable
private fun PollColumn(poll: Poll, onUpdate: OnUpdate, onClickRow: Fn) {
    val isExpired = remember(poll.endsAt) {
        poll.endsAt != null && poll.endsAt <= getCurrentSecs()
    }
    val clickedId = remember {
        mutableStateOf<String?>(null)
    }
    val alreadyVoted = remember(poll) {
        poll.options.any { it.isMyVote }
    }
    val isRevealed = remember(isExpired, alreadyVoted) {
        alreadyVoted || isExpired
    }
    val topVotes = remember(poll) {
        poll.options.maxOfOrNull { it.voteCount } ?: 0
    }
    val totalVotes = remember(poll) {
        poll.options.sumOf { it.voteCount }
    }
    Column {
        for (option in poll.options) {
            PollOptionRow(
                label = option.label,
                isSelected = if (clickedId.value != null) clickedId.value == option.optionId else option.isMyVote,
                isRevealed = isRevealed,
                percentage = remember(option.voteCount, totalVotes) {
                    if (totalVotes == 0) 0
                    else option.voteCount.toFloat().div(totalVotes).times(100).toInt()
                },
                progress = remember(option.voteCount, topVotes) {
                    if (topVotes == 0) 0f else option.voteCount.toFloat().div(topVotes)
                },
                onClick = {
                    if (isRevealed) onClickRow.invoke() else clickedId.value = option.optionId
                },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = spacing.large),
                text = if (totalVotes == 0) stringResource(id = R.string.no_votes)
                else stringResource(id = R.string.n_votes, totalVotes),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(spacing.medium))
            if (isExpired) Text(
                text = stringResource(id = R.string.poll_has_ended),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (!alreadyVoted) clickedId.value?.let { optionId ->
            Button(onClick = {
                onUpdate(VotePollOption(pollId = poll.id, optionId = optionId))
            }) {
                Text(stringResource(id = R.string.vote))
            }
        }
    }
}
