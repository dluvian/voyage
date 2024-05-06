package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.WEBSOCKET_PREFIX
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.launchIO
import kotlinx.coroutines.delay

class RelayProfileViewModel : ViewModel() {
    val relay = mutableStateOf("")
    val isLoading = mutableStateOf(false)

    fun openProfile(relayUrl: RelayUrl) {
        relay.value = relayUrl.removePrefix(WEBSOCKET_PREFIX)
        isLoading.value = true
        viewModelScope.launchIO {
            delay(DELAY_1SEC)
            isLoading.value = false
        }
    }
}
