package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    val title = mutableStateOf("")
    val identifier = mutableStateOf("")

    fun openList(identifier: String, cachedTitle: String) {
        title.value = cachedTitle
        this.identifier.value = identifier
    }
}
