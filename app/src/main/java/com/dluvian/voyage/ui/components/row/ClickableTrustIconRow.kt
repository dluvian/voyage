package com.dluvian.voyage.ui.components.row

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.icon.TrustIcon

Composable () ->Unit
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.icon.TrustIcon

@Composable
fun ClickableTrustIconRow(
    trustType: TrustType,
    header: String? = null,
    content: String? = null,
    trailingContent: @Composable () -> Unit = {},
    onClick: Fn,
) {
    ClickableRow(
        header = header.orEmpty(),
        text = content,
        leadingContent = { TrustIcon(trustType = trustType) },
        trailingContent = trailingContent,
        onClick = onClick
    )
}
