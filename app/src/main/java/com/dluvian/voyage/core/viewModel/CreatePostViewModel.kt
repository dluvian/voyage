package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreatePostViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.SendPoll
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.interactor.PostSender
import kotlinx.coroutines.delay


class CreatePostViewModel(
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
) : ViewModel() {
    val isSending = mutableStateOf(false)

    fun handle(action: CreatePostViewAction) {
        when (action) {
            is SendPost -> sendPost(action = action)
            is SendPoll -> sendPoll(action = action)
        }
    }

    private fun sendPost(action: SendPost) {
        if (isSending.value) return

        isSending.value = true
        viewModelScope.launchIO {
            val result = postSender.sendPost(
                header = action.header,
                body = action.body,
                topics = action.topics,
                isAnon = action.isAnon,
            )

            delay(DELAY_1SEC)
            action.onGoBack()

            result.onSuccess {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.post_created)
                )
            }.onFailure {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.failed_to_create_post)
                )
            }
        }.invokeOnCompletion { isSending.value = false }
    }

    private fun sendPoll(action: SendPoll) {
        if (isSending.value) return

        isSending.value = true
        viewModelScope.launchIO {
            val result = postSender.sendPoll(
                question = action.question,
                options = action.options,
                topics = action.topics,
                isAnon = action.isAnon,
            )

            delay(DELAY_1SEC)
            action.onGoBack()

            result.onSuccess {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.poll_created)
                )
            }.onFailure {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.failed_to_create_poll)
                )
            }
        }.invokeOnCompletion { isSending.value = false }
    }
}
