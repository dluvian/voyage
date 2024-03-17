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
import com.dluvian.voyage.core.SearchText
import com.dluvian.voyage.core.SearchViewAction
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.core.isTopicStr
import com.dluvian.voyage.core.model.Profile
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.provider.TopicProvider
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
    private val maxSearchResult = 5
    val topics = mutableStateOf<List<String>>(emptyList())
    val profiles = mutableStateOf<List<Profile>>(emptyList())

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

    private fun updateSearchText(text: String) {
        topics.value = topicProvider.getAllTopics()
            .filter { it.startsWith(prefix = text, ignoreCase = true) }
            .sortedBy { it.length }
            .take(maxSearchResult)
    }

    private fun searchText(
        text: String,
        context: Context,
        onOpenTopic: (Topic) -> Unit,
        onOpenProfile: (Nip19Profile) -> Unit
    ) {
        val trimmed = text.trim()
        if (trimmed.isTopicStr()) {
            onOpenTopic(trimmed)
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
