package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.PostViewCmd
import com.dluvian.voyage.model.PostViewOpen


class PostViewModel() : ViewModel() {
    val subject = mutableStateOf("")
    val content = mutableStateOf("")
    val topics = mutableStateOf(emptyList<Topic>())

    fun handle(cmd: PostViewCmd) {
        when (cmd) {
            PostViewOpen -> {
                subject.value = ""
                content.value = ""
                topics.value = emptyList()
            }
        }
    }
}
