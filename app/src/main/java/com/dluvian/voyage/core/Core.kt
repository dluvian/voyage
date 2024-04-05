package com.dluvian.voyage.core

import android.util.Log
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.AppContainer
import com.dluvian.voyage.VMContainer
import com.dluvian.voyage.core.model.NeventMention
import com.dluvian.voyage.core.model.NostrMention
import com.dluvian.voyage.core.model.NoteMention
import com.dluvian.voyage.core.model.NprofileMention
import com.dluvian.voyage.core.model.NpubMention
import com.dluvian.voyage.core.navigator.Navigator
import kotlinx.coroutines.launch
import rust.nostr.protocol.Nip19Profile

private const val TAG = "Core"

class Core(
    val vmContainer: VMContainer,
    val appContainer: AppContainer,
    closeApp: Fn,
) : ViewModel() {
    val navigator = Navigator(vmContainer = vmContainer, closeApp = closeApp)
    lateinit var externalSignerHandler: ExternalSignerHandler

    val onUpdate: (UIEvent) -> Unit = { uiEvent ->
        when (uiEvent) {
            is NavEvent -> navigator.handle(action = uiEvent)

            is VoteEvent -> appContainer.postVoter.handle(action = uiEvent)
            is ProfileEvent -> appContainer.profileFollower.handle(action = uiEvent)
            is TopicEvent -> appContainer.topicFollower.handle(action = uiEvent)

            is HomeViewAction -> vmContainer.homeVM.handle(action = uiEvent)
            is DiscoverViewAction -> vmContainer.discoverVM.handle(action = uiEvent)
            is CreatePostViewAction -> vmContainer.createPostVM.handle(action = uiEvent)
            is ThreadViewAction -> vmContainer.threadVM.handle(action = uiEvent)
            is TopicViewAction -> vmContainer.topicVM.handle(action = uiEvent)
            is ProfileViewAction -> vmContainer.profileVM.handle(action = uiEvent)
            is SettingsViewAction -> vmContainer.settingsVM.handle(action = uiEvent)
            is CreateReplyViewAction -> vmContainer.createReplyVM.handle(action = uiEvent)
            is SearchViewAction -> vmContainer.searchVM.handle(action = uiEvent)

            is ProcessExternalSignature -> viewModelScope.launch {
                externalSignerHandler.processExternalSignature(
                    result = uiEvent.activityResult
                )
            }

            is ClickText -> clickText(event = uiEvent)
        }
    }

    @OptIn(ExperimentalTextApi::class)
    private fun clickText(event: ClickText) {
        val url = event.text.getUrlAnnotations(event.offset, event.offset).firstOrNull()
        if (url != null) {
            Log.i("LOLOL", "url")
            event.uriHandler.openUri(url.item.url)
            return
        }

        val other = event.text.getStringAnnotations(event.offset, event.offset).firstOrNull()
        if (other == null) {
            Log.i("LOLOL", "other ${event.offset}")

            if (event.rootPost != null) {
                Log.i("LOLOL", "root")
                onUpdate(OpenThread(rootPost = event.rootPost))
            }
            return
        }

        if (other.item.startsWith("#")) {
            onUpdate(OpenTopic(topic = other.item.removePrefix("#").lowercase()))
            return
        }

        when (val nostrMention = NostrMention.from(other.item)) {
            is NprofileMention -> {
                val nip19 = Nip19Profile.fromBech32(nostrMention.bech32)
                onUpdate(OpenProfile(nprofile = nip19))
            }

            is NpubMention -> {
                val nip19 = createNprofile(hex = nostrMention.hex)
                onUpdate(OpenProfile(nprofile = nip19))
            }

            is NeventMention -> {} // TODO: Implement detached threads
            is NoteMention -> {} // TODO: Implement detached threads
            null -> Log.w(TAG, "Unknown clickable string ${other.item}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        appContainer.nostrService.close()
    }
}
