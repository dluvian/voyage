package com.dluvian.voyage.data.provider

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.ClickProfileSuggestion
import com.dluvian.voyage.core.SearchProfileSuggestion
import com.dluvian.voyage.core.SearchTopicSuggestion
import com.dluvian.voyage.core.SuggestionAction
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.utils.isBareTopicStr
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.normalizeTopic
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class SuggestionProvider(
    private val searchProvider: SearchProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    val profileSuggestions: MutableState<List<AdvancedProfileView>> = mutableStateOf(emptyList())
    val topicSuggestions: MutableState<List<Topic>> = mutableStateOf(emptyList())

    fun handle(action: SuggestionAction) {
        when (action) {
            is ClickProfileSuggestion -> {
                profileSuggestions.value = emptyList()
                scope.launchIO {
                    lazyNostrSubscriber.lazySubNip65(nprofile = createNprofile(hex = action.pubkey))
                }
            }

            is SearchProfileSuggestion -> searchProfile(name = action.name)
            is SearchTopicSuggestion -> searchTopic(topic = action.topic)
        }
    }


    private var profileJob: Job? = null
    private fun searchProfile(name: String) {
        if (name.isBlank()) {
            profileSuggestions.value = emptyList()
            return
        }
        profileJob?.cancel()
        profileJob = scope.launchIO {
            profileSuggestions.value = searchProvider.getProfileSuggestions(text = name)
        }
    }

    private var topicJob: Job? = null
    private fun searchTopic(topic: Topic) {
        val normalized = topic.normalizeTopic()
        if (!normalized.isBareTopicStr()) {
            topicSuggestions.value = emptyList()
            return
        }
        topicJob?.cancel()
        topicJob = scope.launchIO {
            topicSuggestions.value = searchProvider.getTopicSuggestions(text = normalized)
        }
    }
}
