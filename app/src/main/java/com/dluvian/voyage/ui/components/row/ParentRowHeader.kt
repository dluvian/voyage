package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.button.OptionsButton
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.components.icon.BorderedTrustIcon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.components.text.RelativeTime
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ParentRowHeader(
    parent: IParentUI,
    myTopic: String?,
    isOp: Boolean,
    collapsedText: AnnotatedString? = null,
    onUpdate: OnUpdate
) {
    val onOpenProfile = { onUpdate(OpenProfile(nprofile = createNprofile(hex = parent.pubkey))) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClickableTrustIcon(
                pubkey = parent.pubkey,
                trustType = parent.trustType,
                isOp = isOp,
                onClick = onOpenProfile
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
private fun ClickableTrustIcon(
    pubkey: PubkeyHex,
    trustType: TrustType,
    isOp: Boolean,
    onClick: Fn
) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BorderedTrustIcon(
                pubkey = pubkey,
                trustType = trustType,
                height = sizing.smallIndicator
            )
            if (isOp) {
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text = stringResource(id = R.string.op),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OPBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
