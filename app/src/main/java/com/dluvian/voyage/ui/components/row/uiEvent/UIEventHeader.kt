package com.dluvian.voyage.ui.components.row.uiEvent

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dluvian.voyage.R
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FeedCtx
import com.dluvian.voyage.model.OpenProfile
import com.dluvian.voyage.model.OpenTopic
import com.dluvian.voyage.model.ThreadReplyCtx
import com.dluvian.voyage.model.ThreadRootCtx
import com.dluvian.voyage.model.UICtx
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.ui.components.button.OptionsButton
import com.dluvian.voyage.ui.components.icon.ClickableTrustIcon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.components.text.RelativeTime
import com.dluvian.voyage.ui.theme.CrossPostIcon
import com.dluvian.voyage.ui.theme.DenimBlue
import com.dluvian.voyage.ui.theme.OnBgLight
import com.dluvian.voyage.ui.theme.light
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Nip19Profile

@Composable
fun UIEventHeader(
    ctx: UICtx,
    onUpdate: (Cmd) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UIEventHeaderIconsAndName(ctx = ctx, onUpdate = onUpdate)
            if (ctx is ThreadReplyCtx && ctx.isCollapsed) AnnotatedText(
                modifier = Modifier.padding(start = spacing.large),
                text = ctx.uiEvent.annotatedContent,
                maxLines = 1
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ctx.uiEvent.myTopic?.let { topic ->
                BorderedTopic(topic = topic, onUpdate = onUpdate)
                Spacer(modifier = Modifier.width(spacing.large))
            }
            if (ctx !is ThreadReplyCtx || !ctx.isCollapsed) RelativeTime(
                from = ctx.uiEvent.event.createdAt().asSecs().toLong()
            )
            OptionsButton(uiEvent = ctx.uiEvent, onUpdate = onUpdate)
        }
    }
}

@Composable
private fun UIEventHeaderIconsAndName(
    ctx: UICtx,
    onUpdate: (Cmd) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ClickableTrustIcon(
            profile = ctx.uiEvent.authorProfile,
            isOp = when (ctx) {
                is FeedCtx -> false
                is ThreadRootCtx -> true
                is ThreadReplyCtx -> ctx.isOp
            },
            onClick = {
                onUpdate(OpenProfile(Nip19Profile(ctx.uiEvent.event.author())))
            }
        )
        when (ctx.uiEvent.event.kind().asStd()) {
            KindStandard.REPOST, KindStandard.GENERIC_REPOST -> CrossPostIcon(
                crossPost = ctx.uiEvent.inner,
                onUpdate = onUpdate
            )

            else -> {}
        }
    }
}

@Composable
private fun CrossPostIcon(crossPost: UIEvent?, onUpdate: (Cmd) -> Unit) {
    Icon(
        modifier = Modifier
            .size(sizing.smallIndicator)
            .padding(horizontal = spacing.small),
        imageVector = CrossPostIcon,
        contentDescription = stringResource(id = R.string.cross_posted),
        tint = MaterialTheme.colorScheme.onBackground.light(0.6f)
    )
    crossPost?.authorProfile?.let { profile ->
        ClickableTrustIcon(
            profile = profile,
            onClick = {
                onUpdate(OpenProfile(Nip19Profile(profile.pubkey)))
            }
        )
    }
}

@Composable
private fun BorderedTopic(topic: Topic, onUpdate: (Cmd) -> Unit) {
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
            color = OnBgLight,
            overflow = TextOverflow.Ellipsis
        )
    }
}
