package com.dluvian.voyage.core.viewModel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.AddRelay
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.LoadRelays
import com.dluvian.voyage.core.MAX_RELAYS
import com.dluvian.voyage.core.RelayEditorViewAction
import com.dluvian.voyage.core.RemoveRelay
import com.dluvian.voyage.core.SaveRelays
import com.dluvian.voyage.core.ToggleReadRelay
import com.dluvian.voyage.core.ToggleWriteRelay
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.ConnectionStatus
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.ValidatedNip65
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.WEBSOCKET_URI
import com.dluvian.voyage.data.nostr.getNip65s
import com.dluvian.voyage.data.nostr.removeTrailingSlashes
import com.dluvian.voyage.data.nostr.secs
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.tx.Nip65UpsertDao
import kotlinx.coroutines.delay

private const val TAG = "RelayEditorViewModel"

class RelayEditorViewModel(
    val lazyListState: LazyListState,
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
    private val nostrService: NostrService,
    private val nip65UpsertDao: Nip65UpsertDao,
    val connectionStatuses: MutableState<Map<RelayUrl, ConnectionStatus>>
) : ViewModel() {
    val myRelays = mutableStateOf(emptyList<Nip65Relay>())
    val popularRelays = mutableStateOf(emptyList<RelayUrl>())
    val addIsEnabled = mutableStateOf(getAddIsEnabled(myRelays.value))
    val isSaving = mutableStateOf(false)

    fun handle(action: RelayEditorViewAction) {
        when (action) {
            LoadRelays -> loadRelays()
            is AddRelay -> addRelay(action = action)
            is ToggleReadRelay -> toggleRead(relayUrl = action.relayUrl)
            is ToggleWriteRelay -> toggleWrite(relayUrl = action.relayUrl)
            is RemoveRelay -> removeRelay(relayUrl = action.relayUrl)
            is SaveRelays -> saveRelays(action = action)
        }
    }

    private fun loadRelays() {
        myRelays.value = relayProvider.getMyNip65()
        addIsEnabled.value = getAddIsEnabled(myRelays.value)
        viewModelScope.launchIO {
            popularRelays.value = relayProvider.getPopularRelays()
        }
    }

    private fun saveRelays(action: SaveRelays) {
        if (isSaving.value) return

        if (myRelays.value.all { !it.isRead }) {
            snackbar.showToast(
                viewModelScope,
                action.context.getString(R.string.you_dont_have_any_read_relays_configured)
            )
            return
        }

        if (myRelays.value.all { !it.isWrite }) {
            snackbar.showToast(
                viewModelScope,
                action.context.getString(R.string.you_dont_have_any_write_relays_configured)
            )
            return
        }

        isSaving.value = true
        viewModelScope.launchIO {
            val result = nostrService.publishNip65(
                relays = myRelays.value,
                relayUrls = relayProvider.getPublishRelays(),
            )
            if (result.isSuccess) {
                val event = result.getOrThrow()
                val validatedNip65 = ValidatedNip65(
                    pubkey = event.author().toHex(),
                    relays = event.getNip65s(),
                    createdAt = event.createdAt().secs()
                )
                nip65UpsertDao.upsertNip65s(validatedNip65s = listOf(validatedNip65))
            } else {
                Log.w(TAG, "Failed to sign relay list", result.exceptionOrNull())
            }

            delay(DELAY_1SEC)
            action.onGoBack()

            val msgId = if (result.isSuccess) R.string.relay_list_updated
            else R.string.failed_to_sign_relay_list
            snackbar.showToast(viewModelScope, action.context.getString(msgId))
        }.invokeOnCompletion { isSaving.value = false }
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
        val checked = if (relay.startsWith(WEBSOCKET_URI)) relay else WEBSOCKET_URI + relay

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

        nostrService.addRelay(relayUrl = checked)
        myRelays.value += Nip65Relay(url = checked)
        addIsEnabled.value = getAddIsEnabled(myRelays.value)
    }

    private fun String.isWebsocketUrl(): Boolean {
        val regex = Regex("^wss://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9]+)*(:\\d+)?(/\\S*)?\$")
        return regex.matches(this)
    }

    private fun getAddIsEnabled(myRelays: Collection<Nip65Relay>) = myRelays.size < MAX_RELAYS
}
