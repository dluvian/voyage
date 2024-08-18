package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
    leadingIcon: ImageVector?,
    onClick: Fn = {},
    additionalContent: ComposableContent = {},
) {
    val icon = @Composable {
        if (leadingIcon != null) Icon(
            imageVector = leadingIcon,
            contentDescription = header
        )
    }
    ClickableRow(
        header = header,
        text = text,
        leadingContent = if (leadingIcon == null) null else icon,
        onClick = onClick,
        additionalContent = additionalContent
    )
}

@Composable
fun ClickableRow(
    header: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    leadingContent: ComposableContent? = null,
    trailingContent: ComposableContent? = null,
    onClick: Fn = {},
    onLongClick: Fn = {},
    additionalContent: ComposableContent = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ClickableBaseRow(
            header = header,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            text = text,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        additionalContent()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClickableBaseRow(
    header: String,
    leadingContent: ComposableContent? = null,
    trailingContent: ComposableContent? = null,
    text: String? = null,
    onClick: Fn = {},
    onLongClick: Fn = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(spacing.xl))
            }
            Column {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (text != null) {
                    Spacer(modifier = Modifier.height(spacing.small))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(spacing.xl))
            trailingContent()
        }
    }
}
