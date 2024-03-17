package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.SearchViewAction
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.core.model.Profile
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.provider.TopicProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val topicProvider: TopicProvider,
    private val nostrSubscriber: NostrSubscriber
) : ViewModel() {
    private val maxSearchResult = 5
    val topics = mutableStateOf<List<String>>(emptyList())
    val profiles = mutableStateOf<List<Profile>>(emptyList())

    fun handle(searchViewAction: SearchViewAction) {
        when (searchViewAction) {
            is UpdateSearchText -> updateSearchText(text = searchViewAction.text)
        }
    }

    private var profileJob: Job? = null
    fun subProfiles() {
        if (profileJob?.isActive == true) return

        profileJob = viewModelScope.launch(Dispatchers.IO) {
            nostrSubscriber.lazySubWebOfTrustProfiles()
            delay(DELAY_10SEC)
        }
    }

    private fun updateSearchText(text: String) {
        topics.value = topicProvider.getAllTopics()
            .filter { it.startsWith(prefix = text, ignoreCase = true) }
            .sortedBy { it.length }
            .take(maxSearchResult)
    }
}
