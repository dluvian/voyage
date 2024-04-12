package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreateReplyViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.SendReply
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.room.dao.EventRelayDao
import kotlinx.coroutines.delay

class CreateReplyViewModel(
    private val lazyNostrSubscriber: LazyNostrSubscriber,
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
                lazyNostrSubscriber.lazySubNip65(nprofile = createNprofile(hex = newParent.pubkey))
            }
        }

        this.parent.value = newParent
    }

    fun handle(action: CreateReplyViewAction) {
        when (action) {
            is SendReply -> sendReply(action = action)
        }
    }

    private fun sendReply(action: SendReply) {
        if (isSendingReply.value) return

        isSendingReply.value = true
        viewModelScope.launchIO {
            val result = postSender.sendReply(
                parentId = action.parent.id,
                recipient = action.parent.pubkey,
                body = action.body,
                relayHint = eventRelayDao.getEventRelay(eventId = action.parent.id).orEmpty(),
                isTopLevel = action.parent is RootPostUI,
                signerLauncher = action.signerLauncher
            )
            delay(DELAY_1SEC)
            action.onGoBack()
            result.onSuccess {
                snackbar.showToast(viewModelScope, action.context.getString(R.string.reply_created))
            }.onFailure {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.failed_to_create_reply)
                )
            }
        }.invokeOnCompletion { isSendingReply.value = false }
    }
}
