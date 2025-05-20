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
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.Poll
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.MainEventDao
import kotlinx.coroutines.delay
import rust.nostr.sdk.Event

class CreateReplyViewModel(
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
    private val eventRelayDao: EventRelayDao,
    private val mainEventDao: MainEventDao,
) : ViewModel() {
    val isSendingReply = mutableStateOf(false)
    val parent: MutableState<MainEvent?> = mutableStateOf(null)

    fun openParent(newParent: MainEvent) {
        val relevantId = newParent.getRelevantId()
        if (relevantId == this.parent.value?.id) return

        val relevantPubkey = newParent.getRelevantPubkey()
        if (relevantPubkey != this.parent.value?.pubkey) {
            viewModelScope.launchIO {
                lazyNostrSubscriber.lazySubNip65(nprofile = createNprofile(hex = relevantPubkey))
            }
        }
        when (newParent) {
            is LegacyReply, is Comment -> {
                viewModelScope.launchIO {
                    val grandparentAuthor = mainEventDao.getParentAuthor(id = relevantId)
                    if (grandparentAuthor != null && relevantPubkey != grandparentAuthor) {
                        lazyNostrSubscriber.lazySubNip65(createNprofile(hex = grandparentAuthor))
                    }
                }
            }

            is RootPost, is CrossPost, is Poll -> {}
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
            val json = mainEventDao.getJson(id = action.parent.id)

            val result = if (json != null) {
                postSender.sendReply(
                    parent = Event.fromJson(json = json),
                    body = action.body,
                    relayHint = eventRelayDao.getEventRelay(id = action.parent.id)
                        ?.ifEmpty { null },
                    isAnon = action.isAnon
                )
            } else {
                val err = "Can't determine event kind of ${action.parent.getRelevantId()}"
                Result.failure(IllegalStateException(err))
            }

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
