package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ClickableRow(
    header: String,
    text: String? = null,
    imageVector: ImageVector?,
    onClick: Fn = {},
    additionalContent: ComposableContent = {},
) {
    val icon = @Composable {
        if (imageVector != null) Icon(
            imageVector = imageVector,
            contentDescription = header
        )
    }
    ClickableRow(
        header = header,
        text = text,
        icon = if (imageVector == null) null else icon,
        onClick = onClick,
        additionalContent = additionalContent
    )
}

@Composable
fun ClickableRow(
    header: String,
    text: String? = null,
    icon: ComposableContent? = null,
    onClick: Fn = {},
    additionalContent: ComposableContent = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ClickableBaseRow(header = header, icon = icon, text = text, onClick = onClick)
        additionalContent()
    }
}

@Composable
private fun ClickableBaseRow(
    header: String,
    icon: ComposableContent? = null,
    text: String? = null,
    onClick: Fn = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(spacing.xl))
        }
        Column() {
            Text(
                text = header,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (text != null) {
                Spacer(modifier = Modifier.height(spacing.small))
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
