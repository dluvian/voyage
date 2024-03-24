package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.CreatePostViewAction
import com.dluvian.voyage.core.CreatePostViewSendPost
import com.dluvian.voyage.data.interactor.PostSender

class CreatePostViewModel(private val postSender: PostSender) : ViewModel() {
    fun handle(action: CreatePostViewAction) {
        when (action) {
            is CreatePostViewSendPost -> postSender.sendPost(
                header = action.header,
                body = action.body
            )
        }
    }
}
