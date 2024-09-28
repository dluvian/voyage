package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.light
import com.dluvian.voyage.ui.theme.spacing


@Composable
fun ClickableTrustIcon(
    trustType: TrustType,
    isOp: Boolean = false,
    authorName: String? = null,
    onClick: Fn
) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TrustIcon(trustType = trustType, onClick = onClick)
            if (authorName != null) {
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text = authorName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.light()
                )
            }
            if (isOp) {
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text = stringResource(id = R.string.op),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OPBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
