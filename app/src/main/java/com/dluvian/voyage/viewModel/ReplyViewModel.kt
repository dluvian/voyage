package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.ReplyViewCmd
import com.dluvian.voyage.model.ReplyViewOpen
import com.dluvian.voyage.model.UIEvent

class ReplyViewModel() : ViewModel() {
    val parent = mutableStateOf<UIEvent?>(null)
    val reply = mutableStateOf(TextFieldValue(""))

    fun handle(cmd: ReplyViewCmd) {
        when (cmd) {
            is ReplyViewOpen -> {
                parent.value = cmd.uiEvent
                reply.value = TextFieldValue("")
            }
        }
    }
}
