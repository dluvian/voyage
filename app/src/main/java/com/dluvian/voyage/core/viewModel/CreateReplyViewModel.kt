package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreateReplyViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.SendReply
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.data.room.dao.EventRelayDao
import kotlinx.coroutines.delay

class CreateReplyViewModel(
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
    private val eventRelayDao: EventRelayDao,
) : ViewModel() {
    val isSendingReply = mutableStateOf(false)
    val parent: MutableState<ParentUI?> = mutableStateOf(null)

    fun openParent(newParent: ParentUI) {
        val relevantId = newParent.getRelevantId()
        if (relevantId == this.parent.value?.id) return

        val relevantPubkey = newParent.getRelevantPubkey()
        if (relevantPubkey != this.parent.value?.pubkey) {
            viewModelScope.launchIO {
                lazyNostrSubscriber.lazySubNip65(nprofile = createNprofile(hex = relevantPubkey))
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
                parentId = action.parent.getRelevantId(),
                recipient = action.parent.getRelevantPubkey(),
                body = action.body,
                relayHint = eventRelayDao.getEventRelay(eventId = action.parent.id).orEmpty(),
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
