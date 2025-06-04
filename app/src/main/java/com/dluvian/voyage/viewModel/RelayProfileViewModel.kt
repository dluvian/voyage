package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.model.RelayProfileViewCmd
import com.dluvian.voyage.model.RelayProfileViewOpen
import com.dluvian.voyage.nostr.NostrService
import rust.nostr.sdk.RelayInformationDocument

class RelayProfileViewModel(
    private val service: NostrService,
) : ViewModel() {
    val header = mutableStateOf("")
    val profile = mutableStateOf<RelayInformationDocument?>(null)
    val isLoading = mutableStateOf(false)
    private val cache = mutableMapOf<RelayUrl, RelayInformationDocument?>()

    fun handle(cmd: RelayProfileViewCmd) {
        when (cmd) {
            is RelayProfileViewOpen -> TODO()
        }
    }
}
