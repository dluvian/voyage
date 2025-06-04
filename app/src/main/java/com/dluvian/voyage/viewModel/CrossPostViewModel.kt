package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.CrossPostViewCmd
import com.dluvian.voyage.model.CrossPostViewOpen
import rust.nostr.sdk.Event


class CrossPostViewModel : ViewModel() {
    val event = mutableStateOf<Event?>(null)
    val topics = mutableStateOf(emptyList<Topic>())

    fun handle(cmd: CrossPostViewCmd) {
        when (cmd) {
            is CrossPostViewOpen -> {
                topics.value = emptyList()
                event.value = cmd.event
            }
        }
    }
}
