package com.dluvian.voyage.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.CrossPostViewCmd
import com.dluvian.voyage.model.SetEventForCrossPosting
import rust.nostr.sdk.Event


class CrossPostViewModel : ViewModel() {
    val event: MutableState<Event?> = mutableStateOf(null)

    fun handle(cmd: CrossPostViewCmd) {
        when (cmd) {
            is SetEventForCrossPosting -> event.value = cmd.event
        }
    }
}
