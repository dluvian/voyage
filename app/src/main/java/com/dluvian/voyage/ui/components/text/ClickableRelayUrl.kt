package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.OpenRelayProfile

@Composable
fun ClickableRelayUrl(
    relayUrl: RelayUrl,
    onUpdate: (Cmd) -> Unit,
    onClickAddition: () -> Unit = {}
) {
    Text(
        modifier = Modifier.clickable {
            onUpdate(OpenRelayProfile(relayUrl))
            onClickAddition()
        },
        text = relayUrl
    )
}
