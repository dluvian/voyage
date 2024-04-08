package com.dluvian.voyage.core.viewModel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreateReplyViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.SendReply
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.EventRelayDao
import kotlinx.coroutines.delay

class CreateReplyViewModel(
    private val nostrSubscriber: NostrSubscriber,
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
    private val eventRelayDao: EventRelayDao,
) : ViewModel() {
    val isSendingReply = mutableStateOf(false)
    val parent: MutableState<IParentUI?> = mutableStateOf(null)

    fun openParent(newParent: IParentUI) {
        if (newParent.id == this.parent.value?.id) return

        if (newParent.pubkey != this.parent.value?.pubkey) {
            viewModelScope.launchIO {
                nostrSubscriber.subNip65(nprofile = createNprofile(hex = newParent.pubkey))
            }
        }

        this.parent.value = newParent
    }

    fun handle(action: CreateReplyViewAction) {
        when (action) {
            is SendReply -> sendReply(
                parent = action.parent,
                body = action.body,
                context = action.context,
                onGoBack = action.onGoBack
            )
        }
    }

    private fun sendReply(parent: IParentUI, body: String, context: Context, onGoBack: Fn) {
        if (isSendingReply.value) return

        isSendingReply.value = true
        viewModelScope.launchIO {
            val result = postSender.sendReply(
                parentId = parent.id,
                recipient = parent.pubkey,
                body = body,
                relayHint = eventRelayDao.getEventRelay(eventId = parent.id).orEmpty(),
                isTopLevel = parent is RootPostUI,
            )
            delay(DELAY_1SEC)
            onGoBack()
            result.onSuccess {
                snackbar.showToast(viewModelScope, context.getString(R.string.reply_created))
            }.onFailure {
                snackbar.showToast(
                    viewModelScope,
                    context.getString(R.string.failed_to_create_reply)
                )
            }
        }.invokeOnCompletion { isSendingReply.value = false }
    }
}
