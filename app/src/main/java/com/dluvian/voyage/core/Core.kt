package com.dluvian.voyage.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.AppContainer
import com.dluvian.voyage.VMContainer
import com.dluvian.voyage.core.navigator.Navigator
import kotlinx.coroutines.launch

class Core(
    val vmContainer: VMContainer,
    val appContainer: AppContainer,
    closeApp: Fn,
) : ViewModel() {
    val navigator = Navigator(vmContainer = vmContainer, closeApp = closeApp)
    lateinit var externalSignerHandler: ExternalSignerHandler

    val onUpdate: (UIEvent) -> Unit = { uiEvent ->
        when (uiEvent) {
            is NavEvent -> navigator.handle(navEvent = uiEvent)
            is VoteEvent -> appContainer.postVoter.handle(voteEvent = uiEvent)
            is HomeViewAction -> vmContainer.homeVM.handle(action = uiEvent)
            is DiscoverViewAction -> vmContainer.discoverVM.handle(action = uiEvent)
            is TopicViewAction -> vmContainer.topicVM.handle(action = uiEvent)
            is ProfileViewAction -> vmContainer.profileVM.handle(action = uiEvent)
            is SettingsViewAction -> vmContainer.settingsVM.handle(action = uiEvent)
            is SearchViewAction -> vmContainer.searchVM.handle(action = uiEvent)
            is ProcessExternalSignature -> viewModelScope.launch {
                externalSignerHandler.processExternalSignature(
                    result = uiEvent.activityResult
                )
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        appContainer.nostrService.close()
    }
}
