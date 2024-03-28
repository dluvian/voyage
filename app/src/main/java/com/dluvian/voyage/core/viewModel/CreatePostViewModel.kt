package com.dluvian.voyage.core.viewModel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreatePostViewAction
import com.dluvian.voyage.core.CreatePostViewSendPost
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.interactor.PostSender
import kotlinx.coroutines.delay


class CreatePostViewModel(
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
) : ViewModel() {
    val isSendingPost = mutableStateOf(false)
    fun handle(action: CreatePostViewAction) {
        when (action) {
            is CreatePostViewSendPost -> sendPost(
                header = action.header,
                body = action.body,
                context = action.context,
                onGoBack = action.onGoBack,
            )
        }
    }

    private fun sendPost(header: String, body: String, context: Context, onGoBack: Fn) {
        if (isSendingPost.value) return

        isSendingPost.value = true
        viewModelScope.launchIO {
            val result = postSender.sendPost(header = header, body = body)
            delay(DELAY_1SEC)
            onGoBack()
            result.onSuccess {
                snackbar.showToast(
                    viewModelScope,
                    context.getString(R.string.post_sent)
                )
            }.onFailure {
                snackbar.showToast(
                    viewModelScope,
                    context.getString(R.string.failed_to_create_post)
                )
            }
        }.invokeOnCompletion { isSendingPost.value = false }
    }
}
