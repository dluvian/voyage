package com.dluvian.voyage.core

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.data.NostrService
import kotlinx.coroutines.flow.MutableStateFlow

class Core(
    private val nostrService: NostrService
) : ViewModel() {
    val nav = mutableStateOf(listOf("home"))
    val home = MutableStateFlow(UiState())
    val post = MutableStateFlow(UiState())

    fun onUIEvent(uiEvent: UIEvent) {
        when(uiEvent){
            is ClickPost -> nav.value += "post"
            is Refresh -> {}
            is BackPress -> {
                if (nav.value.size > 1) nav
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nostrService.close()
    }
}