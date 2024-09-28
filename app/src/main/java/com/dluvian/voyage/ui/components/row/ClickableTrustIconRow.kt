package com.dluvian.voyage.ui.components.row

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.icon.TrustIcon

@Composable
fun ClickableTrustIconRow(
    trustType: TrustType,
    header: String? = null,
    content: String? = null,
    trailingContent: ComposableContent = {},
    onClick: Fn,
    onTrustIconClick: Fn,
) {
    ClickableRow(
        header = header ?: content.orEmpty(),
        text = if (header != null) content else null,
        leadingContent = {
            TrustIcon(trustType = trustType, onClick = onTrustIconClick)
        },
        trailingContent = trailingContent,
        onClick = onClick
    )
}
