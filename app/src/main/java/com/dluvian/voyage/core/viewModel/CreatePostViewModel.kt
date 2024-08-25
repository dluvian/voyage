package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreatePostViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.interactor.PostSender
import kotlinx.coroutines.delay


class CreatePostViewModel(
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
) : ViewModel() {
    val isSendingPost = mutableStateOf(false)

    fun handle(action: CreatePostViewAction) {
        when (action) {
            is SendPost -> sendPost(action = action)
        }
    }

    private fun sendPost(action: SendPost) {
        if (isSendingPost.value) return

        isSendingPost.value = true
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
        }.invokeOnCompletion { isSendingPost.value = false }
    }
}
