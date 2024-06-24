package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.provider.ItemSetProvider

class ListViewModel(
    val itemSetProvider: ItemSetProvider
) : ViewModel() {
    val isLoading = mutableStateOf(false)

    fun openList(identifier: String) {
        isLoading.value = true
        viewModelScope.launchIO {
            itemSetProvider.loadList(identifier = identifier)
        }.invokeOnCompletion {
            isLoading.value = false
        }
    }
}
