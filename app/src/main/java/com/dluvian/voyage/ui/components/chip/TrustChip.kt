package com.dluvian.voyage.ui.components.chip

import androidx.compose.foundation.clickable
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
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun TrustChip(trustType: TrustType, name: String, onOpenProfile: Fn, isOp: Boolean) {
    Row(
        modifier = Modifier.clickable(onClick = onOpenProfile),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrustIcon(trustType = trustType)
        Spacer(modifier = Modifier.width(spacing.small))
        Text(
            text = name,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
