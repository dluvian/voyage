package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createEmptyNip19Event
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.ThreadViewAction
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.model.ThreadUI
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.data.provider.ThreadProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId

class ThreadViewModel(
    private val threadProvider: ThreadProvider
) : ViewModel() {
    var thread: StateFlow<ThreadUI?> = MutableStateFlow(null)
    val isRefreshing = mutableStateOf(false)

    fun openThread(threadNavView: ThreadNavView) {
        val isSame = threadNavView.nip19Event.eventId().toHex() == thread.value?.rootPost?.id
        val initVal = if (isSame) thread.value else null
        thread = threadProvider.getThread(nip19Event = threadNavView.nip19Event)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initVal)
    }

    fun handle(action: ThreadViewAction) {
        when (action) {
            is ThreadViewRefresh -> refresh(initVal = thread.value)
        }
    }

    private fun refresh(initVal: ThreadUI?) {
        if (isRefreshing.value) return
        val id = initVal?.rootPost?.id ?: return

        isRefreshing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val nip19 = createEmptyNip19Event(eventId = EventId.fromHex(id))
            thread = threadProvider.getThread(nip19Event = nip19)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initVal)
            delay(DELAY_1SEC)
        }.invokeOnCompletion { isRefreshing.value = false }
    }
}
