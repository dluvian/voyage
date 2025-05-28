package com.dluvian.voyage.ui.components.button.footer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.cmd.OpenCrossPostCreation
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.theme.CrossPostIcon

@Composable
fun CrossPostIconButton(relevantId: EventIdHex, onUpdate: OnUpdate) {
    FooterIconButton(
        icon = CrossPostIcon,
        description = stringResource(id = R.string.cross_post),
        onClick = { onUpdate(OpenCrossPostCreation(id = relevantId)) })
}
