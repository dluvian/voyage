package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.navigation.CreatePostNavView
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.Navigator
import com.dluvian.voyage.core.navigation.SettingsNavView
import com.dluvian.voyage.core.navigation.TopicsNavView
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.data.nostr.NostrService

class Core(
    private val nostrService: NostrService,
    val homeViewModel: HomeViewModel,
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
            RefreshHomeView -> homeViewModel.refresh()
            ExpandHomeView -> homeViewModel.append()
            // TODO: Implement Updates
            is ClickDownvote -> {}
            is ClickNeutralizeVote -> {}
            is ClickUpvote -> {}
            is ClickComment -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        nostrService.close()
    }
}