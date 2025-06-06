package com.dluvian.voyage.ui.components.row

import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.TrustProfile




@Composable
fun ClickableProfileRow(
    profile: TrustProfile,
    trailingContent: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    ClickableTrustIconRow(
        profile = profile,
        trailingContent = trailingContent,
        onClick = onClick,
    )
}
