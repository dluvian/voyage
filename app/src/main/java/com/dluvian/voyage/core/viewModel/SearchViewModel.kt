package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.MAX_TOPIC_LEN
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.SearchText
import com.dluvian.voyage.core.SearchViewAction
import com.dluvian.voyage.core.SubUnknownProfiles
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.core.utils.isBareTopicStr
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.normalizeTopic
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.data.nostr.removeMentionChar
import com.dluvian.voyage.data.nostr.removeNostrUri
import com.dluvian.voyage.data.provider.SearchProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.SimplePostView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Nip19Event
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.PublicKey

class SearchViewModel(
    private val searchProvider: SearchProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val snackbar: SnackbarHostState,
) : ViewModel() {
    val topics = mutableStateOf<List<Topic>>(emptyList())
    val profiles = mutableStateOf<List<AdvancedProfileView>>(emptyList())
    val posts = mutableStateOf<List<SimplePostView>>(emptyList())

    fun handle(action: SearchViewAction) {
        when (action) {
            is UpdateSearchText -> updateSearchText(text = action.text)
            is SearchText -> searchText(action)
            is SubUnknownProfiles -> subProfiles()
        }
    }

    private var profileJob: Job? = null
    private fun subProfiles() {
        if (profileJob?.isActive == true) return

        profileJob = viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubUnknownProfiles()
            delay(DELAY_10SEC)
        }
    }

    private var updateJob: Job? = null
    private fun updateSearchText(text: String) {
        updateJob?.cancel()
        updateJob = viewModelScope.launchIO {
            delay(SHORT_DEBOUNCE)
            topics.value = searchProvider.getTopicSuggestions(text = text)
            profiles.value = searchProvider.getProfileSuggestions(text = text)
            posts.value = searchProvider.getPostSuggestions(text = text)
        }
    }

    private fun searchText(action: SearchText) {
        val strippedTopic = searchProvider.getStrippedSearchText(text = action.text)
        if (strippedTopic.length <= MAX_TOPIC_LEN && strippedTopic.isBareTopicStr()) {
            action.onUpdate(OpenTopic(topic = strippedTopic.normalizeTopic()))
            return
        }

        val stripped = action.text.trim().removeNostrUri().removeMentionChar().trim()

        val pubkey = runCatching { PublicKey.parse(stripped) }.getOrNull()
        if (pubkey != null) {
            action.onUpdate(OpenProfile(nprofile = createNprofile(pubkey = pubkey)))
            return
        }

        val nprofile = runCatching { Nip19Profile.fromBech32(bech32 = stripped) }.getOrNull()
        if (nprofile != null) {
            action.onUpdate(OpenProfile(nprofile = nprofile))
            return
        }

        val note1 = runCatching { EventId.parse(stripped) }.getOrNull()
        if (note1 != null) {
            action.onUpdate(OpenThreadRaw(nevent = createNevent(hex = note1.toHex())))
            return
        }

        val nevent = runCatching { Nip19Event.fromBech32(bech32 = stripped) }.getOrNull()
        if (nevent != null) {
            action.onUpdate(OpenThreadRaw(nevent = nevent))
            return
        }

        snackbar.showToast(
            scope = viewModelScope,
            msg = action.context.getString(R.string.invalid_nostr_string)
        )
    }
}
