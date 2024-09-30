package com.dluvian.voyage.ui.components.row.mainEvent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.ui.components.icon.ClickableTrustIcon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.components.text.RelativeTime
import com.dluvian.voyage.ui.theme.CrossPostIcon
import com.dluvian.voyage.ui.theme.DenimBlue
import com.dluvian.voyage.ui.theme.light
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun MainEventHeader(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    onUpdate: OnUpdate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainEventHeaderIconsAndName(ctx = ctx, showAuthor = showAuthorName, onUpdate = onUpdate)
            if (ctx.isCollapsedReply()) AnnotatedText(
                modifier = Modifier.padding(start = spacing.large),
                text = ctx.mainEvent.content,
                maxLines = 1
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            when (val mainEvent = ctx.mainEvent) {
                is RootPost -> mainEvent.myTopic
                is LegacyReply -> null
                is CrossPost -> mainEvent.myTopic
            }?.let { topic ->
                BorderedTopic(topic = topic, onUpdate = onUpdate)
                Spacer(modifier = Modifier.width(spacing.large))
            }
            if (!ctx.isCollapsedReply()) RelativeTime(from = ctx.mainEvent.createdAt)
        }
    }
}

@Composable
private fun MainEventHeaderIconsAndName(
    ctx: MainEventCtx,
    showAuthor: Boolean,
    onUpdate: OnUpdate
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ClickableTrustIcon(
            trustType = ctx.mainEvent.trustType,
            isOp = when (ctx) {
                is FeedCtx -> false
                is ThreadRootCtx -> true
                is ThreadReplyCtx -> ctx.isOp
            },
            authorName = getUiAuthorName(
                showAuthor = showAuthor,
                name = ctx.mainEvent.authorName,
                pubkey = ctx.mainEvent.pubkey
            ),
            onClick = {
                onUpdate(OpenProfile(nprofile = createNprofile(hex = ctx.mainEvent.pubkey)))
            }
        )
        when (val mainEvent = ctx.mainEvent) {
            is CrossPost -> CrossPostIcon(
                showAuthor = showAuthor,
                crossPost = mainEvent,
                onUpdate = onUpdate
            )

            is LegacyReply, is RootPost -> {}
        }
    }
}

@Composable
private fun CrossPostIcon(showAuthor: Boolean, crossPost: CrossPost, onUpdate: OnUpdate) {
    Icon(
        modifier = Modifier
            .size(sizing.smallIndicator)
            .padding(horizontal = spacing.small),
        imageVector = CrossPostIcon,
        contentDescription = stringResource(id = R.string.cross_posted),
        tint = MaterialTheme.colorScheme.onBackground.light(0.6f)
    )
    ClickableTrustIcon(
        trustType = crossPost.crossPostedTrustType,
        authorName = getUiAuthorName(
            showAuthor = showAuthor,
            name = crossPost.crossPostedAuthorName,
            pubkey = crossPost.crossPostedPubkey
        ),
        onClick = {
            onUpdate(OpenProfile(nprofile = createNprofile(hex = crossPost.crossPostedPubkey)))
        }
    )
}

@Composable
private fun BorderedTopic(topic: Topic, onUpdate: OnUpdate) {
    Box(
        modifier = Modifier
            .border(
                border = BorderStroke(width = 1.dp, color = DenimBlue),
                shape = RoundedCornerShape(corner = CornerSize(spacing.medium))
            )
            .clickable(onClick = { onUpdate(OpenTopic(topic = topic)) }),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = spacing.large),
            text = "#$topic",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground.light(),
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Stable
@Composable
private fun getUiAuthorName(showAuthor: Boolean, name: String?, pubkey: PubkeyHex): String {
    return (if (showAuthor) name else null).orEmpty().ifEmpty { pubkey.take(8) }
}
