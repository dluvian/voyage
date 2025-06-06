package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.PostViewCmd
import com.dluvian.voyage.model.PostViewOpen


class PostViewModel() : ViewModel() {
    val subject = mutableStateOf(TextFieldValue())
    val content = mutableStateOf(TextFieldValue())
    val topics = mutableStateOf(emptyList<Topic>())

    fun handle(cmd: PostViewCmd) {
        when (cmd) {
            PostViewOpen -> {
                subject.value = TextFieldValue()
                content.value = TextFieldValue()
                topics.value = emptyList()
            }
        }
    }
}
