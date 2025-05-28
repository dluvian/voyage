package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.cmd.OpenRelayProfile
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.nostr.RelayUrl

@Composable
fun ClickableRelayUrl(relayUrl: RelayUrl, onUpdate: OnUpdate, onClickAddition: Fn = {}) {
    Text(
        modifier = Modifier.clickable {
            onUpdate(OpenRelayProfile(relayUrl = relayUrl))
            onClickAddition()
        },
        text = relayUrl
    )
}
