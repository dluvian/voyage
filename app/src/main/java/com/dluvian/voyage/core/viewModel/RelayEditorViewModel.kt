package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.WEBSOCKET_PREFIX
import com.dluvian.nostr_kt.removeTrailingSlashes
import com.dluvian.voyage.R
import com.dluvian.voyage.core.AddRelay
import com.dluvian.voyage.core.LoadRelays
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.RelayEditorViewAction
import com.dluvian.voyage.core.RemoveRelay
import com.dluvian.voyage.core.SaveRelays
import com.dluvian.voyage.core.ToggleReadRelay
import com.dluvian.voyage.core.ToggleWriteRelay
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.provider.RelayProvider

class RelayEditorViewModel(
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
) : ViewModel() {
    val myRelays = mutableStateOf(emptyList<Nip65Relay>())
    val popularRelays = mutableStateOf(emptyList<RelayUrl>())
    val addIsEnabled = mutableStateOf(getAddIsEnabled(myRelays.value))
    val isSaving = mutableStateOf(false)

    fun handle(action: RelayEditorViewAction) {
        when (action) {
            LoadRelays -> loadRelays()
            SaveRelays -> {}
            is AddRelay -> addRelay(action = action)
            is RemoveRelay -> removeRelay(relayUrl = action.relayUrl)
            is ToggleReadRelay -> toggleRead(relayUrl = action.relayUrl)
            is ToggleWriteRelay -> toggleWrite(relayUrl = action.relayUrl)
        }
    }

    private fun loadRelays() {
        myRelays.value = relayProvider.getMyNip65()
        addIsEnabled.value = getAddIsEnabled(myRelays.value)
        viewModelScope.launchIO {
            popularRelays.value = relayProvider.getPopularRelays()
        }
    }

    private fun removeRelay(relayUrl: RelayUrl) {
        if (myRelays.value.size <= 1) return

        myRelays.value = myRelays.value.filter { it.url != relayUrl }
        addIsEnabled.value = getAddIsEnabled(myRelays.value)
    }

    private fun toggleRead(relayUrl: RelayUrl) {
        myRelays.value = myRelays.value
            .map { if (it.url == relayUrl) it.copy(isRead = !it.isRead) else it }
    }

    private fun toggleWrite(relayUrl: RelayUrl) {
        myRelays.value = myRelays.value
            .map { if (it.url == relayUrl) it.copy(isWrite = !it.isWrite) else it }
    }

    private fun addRelay(action: AddRelay) {
        val relay = action.relayUrl.trim().removeTrailingSlashes()
        val checked = if (relay.startsWith(WEBSOCKET_PREFIX)) relay else WEBSOCKET_PREFIX + relay

        if (myRelays.value.any { it.url == checked }) {
            val err = action.context.getString(R.string.relay_is_already_in_your_list, checked)
            snackbar.showToast(scope = action.scope, msg = err)
            return
        }

        if (!checked.isWebsocketUrl()) {
            val err = action.context.getString(R.string.relay_is_invalid, checked)
            snackbar.showToast(scope = action.scope, msg = err)
            return
        }

        myRelays.value += Nip65Relay(url = checked)
        addIsEnabled.value = getAddIsEnabled(myRelays.value)
    }

    private fun String.isWebsocketUrl(): Boolean {
        val regex = Regex("^wss://[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*(:\\d+)?(/\\S*)?\$")
        return regex.matches(this)
    }

    private fun getAddIsEnabled(myRelays: Collection<Nip65Relay>) = myRelays.size < MAX_RELAYS
}
