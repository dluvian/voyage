package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.WEBSOCKET_PREFIX
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.provider.RelayProfileProvider
import com.dluvian.voyage.data.room.dao.EventRelayDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.RelayInformationDocument

class RelayProfileViewModel(
    private val relayProfileProvider: RelayProfileProvider,
    private val eventRelayDao: EventRelayDao,
) : ViewModel() {
    val header = mutableStateOf("")
    val profile = mutableStateOf<RelayInformationDocument?>(null)
    val isLoading = mutableStateOf(false)
    val postsInDb: MutableState<StateFlow<Int>> = mutableStateOf(MutableStateFlow(0))

    private var job: Job? = null

    fun openProfile(relayUrl: RelayUrl) {
        val noPrefix = relayUrl.removePrefix(WEBSOCKET_PREFIX)
        if (header.value == noPrefix) return

        header.value = noPrefix
        profile.value = null

        isLoading.value = true
        postsInDb.value = eventRelayDao.countByRelay(relayUrl = relayUrl)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
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
