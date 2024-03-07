package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.navigation.CreatePostNavView
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.Navigator
import com.dluvian.voyage.core.navigation.SettingsNavView
import com.dluvian.voyage.core.navigation.TopicsNavView
import com.dluvian.voyage.data.NostrService

class Core(
    private val nostrService: NostrService
) : ViewModel() {
    val navigator = Navigator()
    val snackbarHostState = SnackbarHostState()

    val onUpdate: (UIEvent) -> Unit = { uiEvent ->
        when (uiEvent) {
            is SystemBackPress, GoBack -> navigator.pop()
            ClickCreate -> navigator.push(view = CreatePostNavView)
            ClickHome -> navigator.push(view = HomeNavView)
            ClickInbox -> navigator.push(view = InboxNavView)
            ClickSettings -> navigator.push(view = SettingsNavView)
            ClickTopics -> navigator.push(view = TopicsNavView)
            is ClickDownvote -> TODO()
            is ClickNeutralizeVote -> TODO()
            is ClickUpvote -> TODO()
        }
    }

    override fun onCleared() {
        super.onCleared()
        nostrService.close()
    }
}