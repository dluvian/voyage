package com.dluvian.voyage.data.provider

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.ClickSuggestion
import com.dluvian.voyage.core.ProfileSuggestionAction
import com.dluvian.voyage.core.SearchSuggestion
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ProfileSuggestionProvider(private val searchProvider: SearchProvider) {
    private val scope = CoroutineScope(Dispatchers.IO)
    val suggestions: MutableState<List<AdvancedProfileView>> = mutableStateOf(emptyList())

    fun handle(action: ProfileSuggestionAction) {
        when (action) {
            is ClickSuggestion -> suggestions.value = emptyList()
            is SearchSuggestion -> search(name = action.name)
        }
    }


    private var job: Job? = null
    private fun search(name: String) {
        if (name.isBlank()) return
        job?.cancel()
        job = scope.launchIO {
            suggestions.value = searchProvider.getProfileSuggestions(text = name)
        }
    }
}
