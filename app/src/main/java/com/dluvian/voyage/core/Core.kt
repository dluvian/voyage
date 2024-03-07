package com.dluvian.voyage.core

import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.navigation.Navigator
import com.dluvian.voyage.data.NostrService
import kotlinx.coroutines.flow.MutableStateFlow

class Core(
    private val nostrService: NostrService
) : ViewModel() {
    val navigator = Navigator()
    val post = MutableStateFlow(UiState())

    fun onUIEvent(uiEvent: UIEvent) {
        when(uiEvent){
            is BackPress -> navigator.pop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        nostrService.close()
    }
}