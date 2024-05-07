package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.icon.TrustIcon

@Composable
fun ClickableTrustIconRow(
    trustType: TrustType,
    header: String? = null,
    content: String? = null,
    onClick: Fn,
    onTrustIconClick: Fn,
) {
    ClickableRow(
        header = header ?: content.orEmpty(),
        text = if (header != null) content else null,
        leadingContent = {
            TrustIcon(modifier = Modifier.clickable { onTrustIconClick() }, trustType = trustType)
        },
        onClick = onClick
    )
}
