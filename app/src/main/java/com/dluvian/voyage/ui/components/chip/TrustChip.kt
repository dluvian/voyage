package com.dluvian.voyage.ui.components.chip

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.superLight

@Composable
fun TrustChip(trustType: TrustType, name: String, onOpenProfile: Fn) {
    SmallAssistChip(
        onClick = onOpenProfile,
        label = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { TrustIcon(trustType = trustType) },
        colors = AssistChipDefaults.assistChipColors(containerColor = getTrustColor(trustType = trustType).superLight())
    )
}
