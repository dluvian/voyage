package com.dluvian.voyage.core.viewModel

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreateCrossPostViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SendCrossPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdateCrossPostTopics
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.delay

private const val TAG = "CreateCrossPostViewModel"

class CreateCrossPostViewModel(
    private val topicProvider: TopicProvider,
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
) : ViewModel() {
    val isSending = mutableStateOf(false)
    val myTopics = mutableStateOf(emptyList<Topic>())
    private val id: MutableState<EventIdHex?> = mutableStateOf(null)

    fun prepareCrossPost(id: EventIdHex) {
        this.id.value = id
    }

    fun handle(action: CreateCrossPostViewAction) {
        when (action) {
            is SendCrossPost -> sendCrossPost(action = action)
            is UpdateCrossPostTopics -> updateTopics()
        }
    }

    private fun sendCrossPost(action: SendCrossPost) {
        if (isSending.value) return
        val nonNullId = id.value ?: return

        isSending.value = true
        viewModelScope.launchIO {
            val result = postSender.sendCrossPost(id = nonNullId, topics = action.topics)

            delay(DELAY_1SEC)
            action.onGoBack()

            result.onSuccess {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.cross_post_created)
                )
            }.onFailure {
                Log.w(TAG, "Failed to create cross-post", it)
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.failed_to_create_cross_post)
                )
            }
        }.invokeOnCompletion { isSending.value = false }
    }

    private fun updateTopics() {
        viewModelScope.launchIO {
            myTopics.value = topicProvider.getMyTopics()
        }
    }
}
