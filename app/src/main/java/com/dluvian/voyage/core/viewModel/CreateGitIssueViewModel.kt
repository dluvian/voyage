package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.CreateGitIssueViewAction
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.DLUVIAN_HEX
import com.dluvian.voyage.core.SendGitIssue
import com.dluvian.voyage.core.SubRepoOwnerRelays
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.createNprofile
import kotlinx.coroutines.delay

class CreateGitIssueViewModel(
    private val postSender: PostSender,
    private val snackbar: SnackbarHostState,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) : ViewModel() {
    val isSendingIssue = mutableStateOf(false)

    fun handle(action: CreateGitIssueViewAction) {
        when (action) {
            is SendGitIssue -> sendIssue(action = action)
            SubRepoOwnerRelays -> viewModelScope.launchIO {
                lazyNostrSubscriber.lazySubNip65(createNprofile(hex = DLUVIAN_HEX))
            }
        }
    }

    private fun sendIssue(action: SendGitIssue) {
        if (isSendingIssue.value) return

        isSendingIssue.value = true
        viewModelScope.launchIO {
            val result = postSender.sendGitIssue(
                issue = action.issue,
                isAnon = action.isAnon,
            )

            delay(DELAY_1SEC)
            action.onGoBack()

            result.onSuccess {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.issue_created)
                )
            }.onFailure {
                snackbar.showToast(
                    viewModelScope,
                    action.context.getString(R.string.failed_to_create_issue)
                )
            }
        }.invokeOnCompletion { isSendingIssue.value = false }
    }
}
