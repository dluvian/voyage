package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.model.RelayProfileViewCmd
import com.dluvian.voyage.model.RelayProfileViewOpen
import com.dluvian.voyage.nostr.NostrService
import kotlinx.coroutines.launch
import rust.nostr.sdk.Relay
import rust.nostr.sdk.RelayInformationDocument
import rust.nostr.sdk.nip11GetInformationDocument

class RelayProfileViewModel(
    private val service: NostrService,
) : ViewModel() {
    val header = mutableStateOf("")
    val profile = mutableStateOf<RelayInformationDocument?>(null)
    val isLoading = mutableStateOf(false)
    val relay = mutableStateOf<Relay?>(null)
    private val cache = mutableMapOf<RelayUrl, RelayInformationDocument?>()

    fun handle(cmd: RelayProfileViewCmd) {
        when (cmd) {
            is RelayProfileViewOpen -> {
                header.value = cmd.relayUrl
                profile.value = cache[cmd.relayUrl]
                isLoading.value = profile.value == null

                viewModelScope.launch {
                    relay.value = service.client.relay(cmd.relayUrl)
                    val nip11 = runCatching {
                        nip11GetInformationDocument(cmd.relayUrl)
                    }.getOrNull()
                    if (nip11 != null) {
                        cache[cmd.relayUrl] = nip11
                        profile.value = nip11
                    }
                }.invokeOnCompletion {
                    isLoading.value = false
                }
            }
        }
    }
}
