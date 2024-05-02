package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.CreateCrossPostViewAction
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SendCrossPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdateCrossPostTopics
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.provider.TopicProvider

class CreateCrossPostViewModel(private val topicProvider: TopicProvider) : ViewModel() {
    val isSending = mutableStateOf(false)
    val myTopics = mutableStateOf(emptyList<Topic>())
    private val id: MutableState<EventIdHex?> = mutableStateOf(null)

    fun prepareCrossPost(id: EventIdHex) {
        this.id.value = id
    }

    fun handle(action: CreateCrossPostViewAction) {
        when (action) {
            is SendCrossPost -> TODO()
            is UpdateCrossPostTopics -> updateTopics()
        }
    }

    private fun updateTopics() {
        viewModelScope.launchIO {
            myTopics.value = topicProvider.getMyTopics()
        }
    }
}
