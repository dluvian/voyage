package com.dluvian.voyage.core.viewModel

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.CreateResponseViewAction
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.SendResponse
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.data.nostr.NostrSubscriber

class CreateResponseViewModel(
    private val nostrSubscriber: NostrSubscriber
) : ViewModel() {
    val isSendingResponse = mutableStateOf(false)
    val parent: MutableState<IParentUI?> = mutableStateOf(null)

    fun openParent(parent: IParentUI) {
        if (parent.id == this.parent.value?.id) return

        if (parent.pubkey != this.parent.value?.pubkey) {
            viewModelScope.launchIO {
                nostrSubscriber.subNip65(pubkey = parent.pubkey)
            }
        }

        this.parent.value = parent
    }

    fun handle(action: CreateResponseViewAction) {
        when (action) {
            is SendResponse -> sendResponse(
                body = action.body,
                context = action.context,
                onGoBack = action.onGoBack
            )
        }
    }

    private fun sendResponse(body: String, context: Context, onGoBack: Fn) {
        TODO()
    }
}
