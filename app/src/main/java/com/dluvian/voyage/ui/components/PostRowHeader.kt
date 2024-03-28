package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.components.chip.TrustChip
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.components.text.RelativeTime
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun PostRowHeader(
    trustType: TrustType,
    authorName: String,
    pubkey: PubkeyHex,
    isDetailed: Boolean,
    createdAt: Long,
    myTopic: String?,
    collapsedText: String? = null,
    onUpdate: OnUpdate
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (isDetailed) TrustChip(
            trustType = trustType,
            name = authorName,
            onOpenProfile = { onUpdate(OpenProfile(nprofile = createNprofile(hex = pubkey))) }
        ) else TrustIcon(trustType = trustType)
        myTopic?.let { topic ->
            TopicChip(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .padding(start = spacing.large),
                topic = topic
            )
        }
        Spacer(modifier = Modifier.width(spacing.large))
        if (collapsedText == null) RelativeTime(from = createdAt)
        else Text(text = collapsedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
