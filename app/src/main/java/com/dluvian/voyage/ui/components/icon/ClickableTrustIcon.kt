package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.model.OneselfProfile
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.OnBgLight
import com.dluvian.voyage.ui.theme.spacing


@Composable
fun ClickableTrustIcon(
    profile: TrustProfile,
    isOp: Boolean = false,
    onClick: () -> Unit
) {
    val isOneself = remember(profile) { profile is OneselfProfile }
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isOneself) Spacer(modifier = Modifier.width(spacing.tiny))
            TrustIcon(profile)
            if (!isOneself) Spacer(modifier = Modifier.width(spacing.medium))
            Text(
                text = profile.uiName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = OnBgLight
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
}
