package com.dluvian.voyage.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.CrossPostViewCmd
import com.dluvian.voyage.model.PushEventForCrossPosting
import rust.nostr.sdk.Event

private const val TAG = "CreateCrossPostViewModel"

class CrossPostViewModel : ViewModel() {
    private val event: MutableState<Event?> = mutableStateOf(null)

    fun handle(cmd: CrossPostViewCmd) {
        when (cmd) {
            is PushEventForCrossPosting -> event.value = cmd.event
        }
    }
}
