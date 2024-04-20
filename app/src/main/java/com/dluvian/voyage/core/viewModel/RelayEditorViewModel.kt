package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.AddRelay
import com.dluvian.voyage.core.LoadRelays
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.RelayEditorViewAction
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.provider.RelayProvider

class RelayEditorViewModel(
    private val relayProvider: RelayProvider
) : ViewModel() {
    val myRelays = mutableStateOf(emptyList<Nip65Relay>())
    val popularRelays = mutableStateOf(emptyList<RelayUrl>())
    val addIsEnabled = mutableStateOf(myRelays.value.size < MAX_RELAYS)
    val isError = mutableStateOf(false)

    fun handle(action: RelayEditorViewAction) {
        when (action) {
            is AddRelay -> {}
            LoadRelays -> loadRelays()
        }
    }

    private fun loadRelays() {
        myRelays.value = relayProvider.getMyNip65()
        viewModelScope.launchIO {
            popularRelays.value = relayProvider.getPopularRelays()
        }
    }
}
