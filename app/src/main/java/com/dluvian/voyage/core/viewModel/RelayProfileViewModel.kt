package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.WEBSOCKET_PREFIX
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.provider.RelayProfileProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import rust.nostr.protocol.RelayInformationDocument

class RelayProfileViewModel(private val relayProfileProvider: RelayProfileProvider) : ViewModel() {
    val header = mutableStateOf("")
    val profile = mutableStateOf<RelayInformationDocument?>(null)
    val isLoading = mutableStateOf(false)

    private var job: Job? = null

    fun openProfile(relayUrl: RelayUrl) {
        val noPrefix = relayUrl.removePrefix(WEBSOCKET_PREFIX)
        if (header.value == noPrefix) return

        header.value = noPrefix
        profile.value = null

        isLoading.value = true
        job?.cancel()
        job = viewModelScope.launchIO {
            val httpsUrl = "https://$noPrefix"
            val fromNetwork = relayProfileProvider.getRelayProfile(httpsUrl = httpsUrl)
            profile.value = fromNetwork
            delay(DELAY_1SEC)
        }
        job?.invokeOnCompletion { isLoading.value = false }
    }
}
