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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ClickableRow(
    header: String,
    text: String? = null,
    leadingIcon: ImageVector?,
    iconTint: Color = LocalContentColor.current,
    onClick: () -> Unit = {},
    additionalContent: @Composable () -> Unit = {},
) {
    val icon = @Composable {
        if (leadingIcon != null) Icon(
            imageVector = leadingIcon,
            tint = iconTint,
            contentDescription = header
        )
    }
    ClickableRow(
        header = header,
        text = text,
        leadingContent = if (leadingIcon == null) {
            {}
        } else {
            icon
        },
        onClick = onClick,
        additionalContent = additionalContent
    )
}

@Composable
fun ClickableRow(
    header: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    leadingContent: @Composable () -> Unit = { },
    trailingContent: @Composable () -> Unit = { },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    additionalContent: @Composable () -> Unit = {},
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

typealias ComposableContent = @Composable () -> Unit

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClickableBaseRow(
    header: String,
    leadingContent: ComposableContent? = null,
    trailingContent: ComposableContent? = null,
    text: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
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
                if (header.isNotEmpty()) Text(
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
