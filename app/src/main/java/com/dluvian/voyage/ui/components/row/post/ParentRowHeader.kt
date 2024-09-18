package com.dluvian.voyage.ui.components.row.post

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
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.FeedItemUI
import com.dluvian.voyage.core.model.RootPostUI
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
fun ParentRowHeader(
    parent: FeedItemUI,
    myTopic: String?,
    isOp: Boolean,
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
            CrossPostedTrustIcons(
                parent = parent,
                isOp = isOp,
                showAuthor = showAuthorName,
                onUpdate = onUpdate
            )
            myTopic?.let { topic ->
                TopicChip(
                    modifier = Modifier
                        .weight(weight = 1f, fill = false)
                        .padding(start = spacing.large),
                    topic = topic,
                    onClick = { onUpdate(OpenTopic(topic = topic)) },
                )
            }
            Spacer(modifier = Modifier.width(spacing.large))
            if (collapsedText == null) RelativeTime(from = parent.createdAt)
            else AnnotatedText(
                text = collapsedText,
                maxLines = 1,
                onClick = { onUpdate(ThreadViewToggleCollapse(id = parent.id)) }
            )
        }
        Row(horizontalArrangement = Arrangement.End) {
            OptionsButton(parent = parent, onUpdate = onUpdate)
        }
    }
}

@Composable
private fun CrossPostedTrustIcons(
    parent: FeedItemUI,
    isOp: Boolean,
    showAuthor: Boolean,
    onUpdate: OnUpdate
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ClickableTrustIcon(
            trustType = parent.trustType,
            isOp = isOp,
            authorName = if (showAuthor) parent.authorName else null,
            onClick = { onUpdate(OpenProfile(nprofile = createNprofile(hex = parent.pubkey))) }
        )
        if (parent is RootPostUI &&
            parent.crossPostedPubkey != null &&
            parent.crossPostedTrustType != null
        ) {
            Icon(
                modifier = Modifier.size(sizing.smallIndicator),
                imageVector = CrossPostIcon,
                contentDescription = stringResource(id = R.string.cross_posted)
            )
            ClickableTrustIcon(
                trustType = parent.crossPostedTrustType,
                onClick = {
                    onUpdate(OpenProfile(nprofile = createNprofile(hex = parent.crossPostedPubkey)))
                }
            )
        }
    }
}
