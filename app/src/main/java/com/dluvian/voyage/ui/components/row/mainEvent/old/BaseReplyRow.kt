package com.dluvian.voyage.ui.components.row.mainEvent.old

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenReplyCreation
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.ui.components.row.mainEvent.FeedItemHeader
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.ReplyIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun BaseReplyRow(
    reply: LegacyReply,
    isCollapsed: Boolean,
    showFullReplyButton: Boolean,
    isOp: Boolean,
    isThread: Boolean,
    onUpdate: OnUpdate,
    showAuthorName: Boolean,
    onToggleCollapse: Fn = {},
    additionalStartAction: ComposableContent = {},
) {
    val onClick = {
        if (isThread) {
            onToggleCollapse()
        } else {
            onUpdate(OpenThreadRaw(nevent = createNevent(hex = reply.getRelevantId())))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(spacing.screenEdge)
    ) {
        FeedItemHeader(
            ctx = reply,
            myTopic = null,
            isOp = isOp,
            showAuthorName = showAuthorName,
            collapsedText = if (isCollapsed) reply.content else null,
            onUpdate = onUpdate
        )
        AnimatedVisibility(
            visible = !isCollapsed,
            exit = slideOutVertically(animationSpec = tween(durationMillis = 0))
        ) {
            Spacer(modifier = Modifier.height(spacing.large))
            val uriHandler = LocalUriHandler.current
            AnnotatedText(
                text = reply.content,
                maxLines = Int.MAX_VALUE,
                onClick = { offset ->
                    onUpdate(
                        ClickText(
                            text = reply.content,
                            offset = offset,
                            uriHandler = uriHandler,
                            onNoneClick = onClick
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }
        if (!isCollapsed) FeedItemActions(
            postId = reply.id,
            authorPubkey = reply.pubkey,
            isUpvoted = reply.isUpvoted,
            upvoteCount = reply.upvoteCount,
            isBookmarked = reply.isBookmarked,
            onUpdate = onUpdate,
            additionalStartAction = additionalStartAction,
            additionalEndAction = {
                TextButton(
                    modifier = Modifier.height(ButtonDefaults.MinHeight),
                    onClick = { onUpdate(OpenReplyCreation(parent = reply)) }) {
                    Icon(
                        imageVector = ReplyIcon,
                        contentDescription = stringResource(id = R.string.reply)
                    )
                    if (showFullReplyButton) Text(text = stringResource(id = R.string.reply))
                }
            }
        )
    }
}
