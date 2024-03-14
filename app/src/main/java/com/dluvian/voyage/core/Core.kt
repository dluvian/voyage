package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.navigator.Navigator
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.nostr.NostrService
import kotlinx.coroutines.launch

class Core(
    val homeViewModel: HomeViewModel,
    val settingsViewModel: SettingsViewModel,
    val snackbar: SnackbarHostState,
    private val postVoter: PostVoter,
    private val nostrService: NostrService,
    closeApp: Fn,
) : ViewModel() {
    val navigator = Navigator(closeApp = closeApp)
    lateinit var externalSignerHandler: ExternalSignerHandler

    val onUpdate: (UIEvent) -> Unit = { uiEvent ->
        when (uiEvent) {
            is NavEvent -> navigator.handle(navEvent = uiEvent)
            is VoteEvent -> postVoter.handle(voteEvent = uiEvent)
            is HomeViewAction -> homeViewModel.handle(homeViewAction = uiEvent)
            is SettingsViewAction -> settingsViewModel.handle(settingsViewAction = uiEvent)
            is ClickThread -> {} // TODO: Click thread
            is ProcessExternalSignature -> viewModelScope.launch {
                externalSignerHandler.processExternalSignature(
                    result = uiEvent.activityResult
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nostrService.close()
    }
}
