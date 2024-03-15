package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.SearchViewAction
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.core.model.Profile
import com.dluvian.voyage.data.provider.TopicProvider

class SearchViewModel(private val topicProvider: TopicProvider) : ViewModel() {
    private val maxSearchResult = 5
    val topics = mutableStateOf<List<String>>(emptyList())
    val profiles = mutableStateOf<List<Profile>>(emptyList())

    fun handle(searchViewAction: SearchViewAction) {
        when (searchViewAction) {
            is UpdateSearchText -> updateSearchText(text = searchViewAction.text)
        }
    }

    fun subProfiles() {

    }

    private fun updateSearchText(text: String) {
        topics.value = topicProvider.getAllTopics()
            .filter { it.startsWith(prefix = text, ignoreCase = true) }
            .sortedBy { it.length }
            .take(maxSearchResult)
    }


}