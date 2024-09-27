package com.dluvian.voyage.ui.components.row.mainEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.ui.components.button.OptionsButton
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.components.icon.ClickableTrustIcon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.components.text.RelativeTime
import com.dluvian.voyage.ui.theme.CrossPostIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun MainEventHeader(
    ctx: MainEventCtx,
    showAuthorName: Boolean,
    collapsedText: AnnotatedString? = null,
    onUpdate: OnUpdate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedItemHeaderTrustIcons(
                ctx = ctx,
                showAuthor = showAuthorName,
                onUpdate = onUpdate
            )
            when (val mainEvent = ctx.mainEvent) {
                is RootPost -> mainEvent.myTopic
                is LegacyReply -> null
                is CrossPost -> mainEvent.myTopic
            }?.let { topic ->
                TopicChip(
                    modifier = Modifier
                        .weight(weight = 1f, fill = false)
                        .padding(start = spacing.large),
                    topic = topic,
                    onClick = { onUpdate(OpenTopic(topic = topic)) },
                )
            }
            Spacer(modifier = Modifier.width(spacing.large))
            if (collapsedText == null) RelativeTime(from = ctx.mainEvent.createdAt)
            else AnnotatedText(text = collapsedText, maxLines = 1)
        }
        Row(horizontalArrangement = Arrangement.End) {
            OptionsButton(mainEvent = ctx.mainEvent, onUpdate = onUpdate)
        }
    }
}

@Composable
private fun FeedItemHeaderTrustIcons(
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
            authorName = if (showAuthor) ctx.mainEvent.authorName else null,
            onClick = {
                onUpdate(OpenProfile(nprofile = createNprofile(hex = ctx.mainEvent.pubkey)))
            }
        )
        when (val mainEvent = ctx.mainEvent) {
            is CrossPost -> CrossPostIcon(crossPost = mainEvent, onUpdate = onUpdate)
            is LegacyReply, is RootPost -> {}
        }
    }
}

@Composable
private fun CrossPostIcon(crossPost: CrossPost, onUpdate: OnUpdate) {
    Icon(
        modifier = Modifier.size(sizing.smallIndicator),
        imageVector = CrossPostIcon,
        contentDescription = stringResource(id = R.string.cross_posted)
    )
    ClickableTrustIcon(
        trustType = crossPost.crossPostedTrustType,
        onClick = {
            onUpdate(OpenProfile(nprofile = createNprofile(hex = crossPost.crossPostedPubkey)))
        }
    )
}
