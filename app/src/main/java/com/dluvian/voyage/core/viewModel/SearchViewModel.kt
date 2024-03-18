package com.dluvian.voyage.core.viewModel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.removeMentionChar
import com.dluvian.nostr_kt.removeNostrUri
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.MAX_TOPIC_LEN
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.SearchText
import com.dluvian.voyage.core.SearchViewAction
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.core.isBareTopicStr
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.room.entity.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

class SearchViewModel(
    private val topicProvider: TopicProvider,
    private val nostrSubscriber: NostrSubscriber,
    private val snackbar: SnackbarHostState
) : ViewModel() {
    private val maxSearchResult = 7
    val topics = mutableStateOf<List<String>>(emptyList())
    val profiles = mutableStateOf<List<ProfileEntity>>(emptyList())

    fun handle(searchViewAction: SearchViewAction) {
        when (searchViewAction) {
            is UpdateSearchText -> updateSearchText(text = searchViewAction.text)
            is SearchText -> searchText(
                text = searchViewAction.text,
                context = searchViewAction.context,
                onOpenTopic = searchViewAction.onOpenTopic,
                onOpenProfile = searchViewAction.onOpenProfile
            )
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

    private var updateJob: Job? = null
    private fun updateSearchText(text: String) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            delay(SHORT_DEBOUNCE)
            val trimmed = text.trim().dropWhile { it == '#' }.trim().lowercase()
            topics.value = getTopicSuggestions(text = trimmed)
        }
    }

    private fun getTopicSuggestions(text: String): List<Topic> {
        val suggestions = topicProvider.getAllTopics()
            .asSequence()
            .filter { it.contains(other = text, ignoreCase = true) }
            .sortedBy { it.length }
            .distinctBy { it.lowercase() }
            .take(maxSearchResult)
            .toList()

        return if (text.length > MAX_TOPIC_LEN ||
            !text.isBareTopicStr() ||
            suggestions.contains(text)
        ) suggestions
        else mutableListOf(text) + suggestions
    }

    private fun searchText(
        text: String,
        context: Context,
        onOpenTopic: (Topic) -> Unit,
        onOpenProfile: (Nip19Profile) -> Unit
    ) {
        if (topics.value.isNotEmpty()) {
            onOpenTopic(topics.value.first())
            return
        }
        if (profiles.value.isNotEmpty()) {
            onOpenProfile(profiles.value.first().toNip19())
            return
        }
        if (text.isBareTopicStr()) {
            onOpenTopic(text.lowercase())
            return
        }

        val stripped = text.trim().removeNostrUri().removeMentionChar().trim()

        val pubkey = runCatching { PublicKey.fromBech32(bech32 = stripped) }.getOrNull()
        if (pubkey != null) {
            onOpenProfile(Nip19Profile(publicKey = pubkey, relays = emptyList()))
            return
        }
        val nprofile = runCatching { Nip19Profile.fromBech32(bech32 = stripped) }.getOrNull()
        if (nprofile != null) {
            onOpenProfile(nprofile)
            return
        }
        snackbar.showToast(
            scope = viewModelScope,
            msg = context.getString(R.string.invalid_npub_nprofile)
        )
    }
}
