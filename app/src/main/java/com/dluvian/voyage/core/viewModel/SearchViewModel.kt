package com.dluvian.voyage.core.viewModel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNprofile
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
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.normalizeTopic
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.SuggestionProvider
import com.dluvian.voyage.data.room.entity.ProfileEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

class SearchViewModel(
    private val suggestionProvider: SuggestionProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val snackbar: SnackbarHostState
) : ViewModel() {
    val topics = mutableStateOf<List<Topic>>(emptyList())
    val profiles = mutableStateOf<List<ProfileEntity>>(emptyList())

    fun handle(action: SearchViewAction) {
        when (action) {
            is UpdateSearchText -> updateSearchText(text = action.text)
            is SearchText -> searchText(
                text = action.text,
                context = action.context,
                onOpenTopic = action.onOpenTopic,
                onOpenProfile = action.onOpenProfile
            )
        }
    }

    private var profileJob: Job? = null
    fun subProfiles() {
        if (profileJob?.isActive == true) return

        profileJob = viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubWebOfTrustProfiles()
            delay(DELAY_10SEC)
        }
    }

    private fun updateSearchText(text: String) {
        updateTopicSuggestions(text = text)
        updateProfileSuggestions(text = text)
    }

    private var updateJobTopic: Job? = null
    private fun updateTopicSuggestions(text: String) {
        updateJobTopic?.cancel()
        updateJobTopic = viewModelScope.launchIO {
            delay(SHORT_DEBOUNCE)
            topics.value = suggestionProvider.getTopicSuggestions(text = text)
        }
    }

    private var updateJobProfile: Job? = null
    private fun updateProfileSuggestions(text: String) {
        updateJobProfile?.cancel()
        updateJobProfile = viewModelScope.launchIO {
            delay(SHORT_DEBOUNCE)
            profiles.value = suggestionProvider.getProfileSuggestions(text = text)
        }
    }

    private fun searchText(
        text: String,
        context: Context,
        onOpenTopic: (Topic) -> Unit,
        onOpenProfile: (Nip19Profile) -> Unit
    ) {
        val strippedTopic = suggestionProvider.getStrippedSearchText(text = text)
        if (strippedTopic.length <= MAX_TOPIC_LEN && strippedTopic.isBareTopicStr()) {
            onOpenTopic(strippedTopic.normalizeTopic())
            return
        }

        val stripped = text.trim().removeNostrUri().removeMentionChar().trim()

        val pubkey = runCatching { PublicKey.fromBech32(bech32 = stripped) }.getOrNull()
        if (pubkey != null) {
            onOpenProfile(createNprofile(pubkey = pubkey))
            return
        }

        val nprofile = runCatching { Nip19Profile.fromBech32(bech32 = stripped) }.getOrNull()
        if (nprofile != null) {
            onOpenProfile(nprofile)
            return
        }

        snackbar.showToast(
            scope = viewModelScope,
            msg = context.getString(R.string.invalid_search)
        )
    }
}
