package com.dluvian.voyage.ui.components.row

import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.icon.TrustIcon

@Composable
fun ClickableTrustIconRow(
    profile: TrustProfile,
    content: String? = null,
    trailingContent: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    ClickableRow(
        header = profile.uiName(),
        text = content,
        leadingContent = { TrustIcon(profile) },
        trailingContent = trailingContent,
        onClick = onClick
    )
}
