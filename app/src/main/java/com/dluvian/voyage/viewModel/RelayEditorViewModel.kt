package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.model.AddRelay
import com.dluvian.voyage.model.RelayEditorViewCmd
import com.dluvian.voyage.model.RelayEditorViewOpen
import com.dluvian.voyage.model.RemoveRelay
import com.dluvian.voyage.model.ToggleReadRelay
import com.dluvian.voyage.model.ToggleWriteRelay
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.provider.IEventUpdate
import rust.nostr.sdk.Event
import rust.nostr.sdk.RelayMetadata

class RelayEditorViewModel(
    val lazyListState: LazyListState,
    private val service: NostrService,
) : ViewModel(), IEventUpdate {
    val isEditing = mutableStateOf(false)
    val nip65 = mutableStateOf(emptyList<Pair<RelayUrl, RelayMetadata?>>())
    val gossipRelays = mutableStateOf(emptyList<RelayUrl>())
    val addIsEnabled = mutableStateOf(false)

    fun handle(action: RelayEditorViewCmd) {
        when (action) {
            RelayEditorViewOpen -> TODO()
            is AddRelay -> TODO()
            is RemoveRelay -> TODO()
            is ToggleReadRelay -> TODO()
            is ToggleWriteRelay -> TODO()
        }
    }

    override suspend fun update(event: Event) {
        TODO("Not yet implemented")
    }
}
