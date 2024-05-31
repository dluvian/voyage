package com.dluvian.voyage.core

import android.content.Intent
import android.util.Log
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNevent
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
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile

private const val TAG = "Core"

class Core(
    val vmContainer: VMContainer,
    val appContainer: AppContainer,
    closeApp: Fn,
) : ViewModel() {
    val navigator = Navigator(vmContainer = vmContainer, closeApp = closeApp)

    private var lastDeeplink = ""
    fun handleDeeplink(intent: Intent) {
        if (intent.scheme != "nostr") return
        val nostrString = intent.data?.schemeSpecificPart ?: return
        if (lastDeeplink == nostrString) return
        lastDeeplink = nostrString
        openNostrString(str = nostrString)
    }

    val onUpdate: (UIEvent) -> Unit = { uiEvent ->
        when (uiEvent) {
            is NavEvent -> navigator.handle(action = uiEvent)

            is VoteEvent -> appContainer.postVoter.handle(action = uiEvent)
            is ProfileEvent -> appContainer.profileFollower.handle(action = uiEvent)
            is TopicEvent -> appContainer.topicFollower.handle(action = uiEvent)
            is DeletePost -> viewModelScope.launchIO {
                appContainer.eventDeletor.deletePost(postId = uiEvent.id)
            }

            is HomeViewAction -> vmContainer.homeVM.handle(action = uiEvent)
            is DiscoverViewAction -> vmContainer.discoverVM.handle(action = uiEvent)
            is ThreadViewAction -> vmContainer.threadVM.handle(action = uiEvent)
            is TopicViewAction -> vmContainer.topicVM.handle(action = uiEvent)
            is ProfileViewAction -> vmContainer.profileVM.handle(action = uiEvent)
            is SettingsViewAction -> vmContainer.settingsVM.handle(action = uiEvent)
            is CreatePostViewAction -> vmContainer.createPostVM.handle(action = uiEvent)
            is CreateReplyViewAction -> vmContainer.createReplyVM.handle(action = uiEvent)
            is CreateCrossPostViewAction -> vmContainer.createCrossPostVM.handle(action = uiEvent)
            is SearchViewAction -> vmContainer.searchVM.handle(action = uiEvent)
            is EditProfileViewAction -> vmContainer.editProfileVM.handle(action = uiEvent)
            is RelayEditorViewAction -> vmContainer.relayEditorVM.handle(action = uiEvent)
            is InboxViewAction -> vmContainer.inboxVM.handle(action = uiEvent)

            is ProcessExternalSignature -> viewModelScope.launch {
                appContainer.externalSignerHandler.processExternalSignature(
                    result = uiEvent.activityResult
                )
            }

            is ClickText -> clickText(action = uiEvent)

            is ProfileSuggestionAction -> appContainer.profileSuggestionProvider.handle(action = uiEvent)
            is RegisterAccountLauncher -> appContainer.externalSignerHandler.setAccountLauncher(
                launcher = uiEvent.launcher
            )

            is RegisterSignerLauncher -> appContainer.externalSignerHandler.setSignerLauncher(
                launcher = uiEvent.launcher
            )
        }
    }

    @OptIn(ExperimentalTextApi::class)
    private fun clickText(action: ClickText) {
        val url = action.text.getUrlAnnotations(action.offset, action.offset).firstOrNull()
        if (url != null) {
            action.uriHandler.openUri(url.item.url)
            return
        }

        val other = action.text.getStringAnnotations(action.offset, action.offset).firstOrNull()
        if (other == null) {
            action.onNoneClick()
            return
        }

        if (other.item.startsWith("#")) {
            onUpdate(OpenTopic(topic = other.item.normalizeTopic()))
            return
        }

        openNostrString(str = other.item)
    }

    private fun openNostrString(str: String) {
        when (val nostrMention = NostrMention.from(str)) {
            is NprofileMention -> {
                val nip19 = Nip19Profile.fromBech32(nostrMention.bech32)
                onUpdate(OpenProfile(nprofile = nip19))
            }

            is NpubMention -> {
                val nprofile = createNprofile(hex = nostrMention.hex)
                onUpdate(OpenProfile(nprofile = nprofile))
            }

            is NeventMention -> {
                onUpdate(OpenThreadRaw(nevent = Nip19Event.fromBech32(nostrMention.bech32)))
            }

            is NoteMention -> {
                val nevent = createNevent(hex = nostrMention.hex)
                onUpdate(OpenThreadRaw(nevent = nevent))
            }

            null -> Log.w(TAG, "Unknown clickable string $str")
        }
    }

    override fun onCleared() {
        super.onCleared()
        appContainer.nostrService.close()
    }
}
