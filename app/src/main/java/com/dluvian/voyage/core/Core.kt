package com.dluvian.voyage.core

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.AppContainer
import com.dluvian.voyage.R
import com.dluvian.voyage.VMContainer
import com.dluvian.voyage.core.model.CoordinateMention
import com.dluvian.voyage.core.model.ItemSetProfile
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.core.model.NeventMention
import com.dluvian.voyage.core.model.NostrMention
import com.dluvian.voyage.core.model.NoteMention
import com.dluvian.voyage.core.model.NprofileMention
import com.dluvian.voyage.core.model.NpubMention
import com.dluvian.voyage.core.model.RelayMention
import com.dluvian.voyage.core.navigator.Navigator
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.normalizeTopic
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.core.utils.textNoteAndRepostKinds
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.nostr.createNprofile
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

            is DrawerViewAction -> vmContainer.drawerVM.handle(action = uiEvent)

            is VoteEvent -> appContainer.postVoter.handle(action = uiEvent)
            is ProfileEvent -> appContainer.profileFollower.handle(action = uiEvent)
            is TopicEvent -> appContainer.topicFollower.handle(action = uiEvent)
            is DeletePost -> viewModelScope.launchIO {
                appContainer.eventDeletor.deletePost(postId = uiEvent.id)
            }

            is DeleteList -> viewModelScope.launchIO {
                appContainer.eventDeletor.deleteList(
                    identifier = uiEvent.identifier,
                    onCloseDrawer = uiEvent.onCloseDrawer
                )
            }

            is AddItemToList -> viewModelScope.launchIO {
                appContainer.itemSetEditor.addItemToSet(
                    item = uiEvent.item,
                    identifier = uiEvent.identifier
                ).onFailure {
                    val errId = when (uiEvent.item) {
                        is ItemSetProfile -> R.string.failed_to_sign_profile_list
                        is ItemSetTopic -> R.string.failed_to_sign_topic_list
                    }
                    appContainer.snackbar.showToast(
                        scope = uiEvent.scope,
                        msg = uiEvent.context.getString(errId)
                    )
                }.onSuccess {
                    appContainer.snackbar.showToast(
                        scope = uiEvent.scope,
                        msg = uiEvent.context.getString(R.string.added_to_list)
                    )
                }
            }

            is BookmarkEvent -> appContainer.bookmarker.handle(action = uiEvent)
            is MuteEvent -> appContainer.muter.handle(action = uiEvent)

            is HomeViewAction -> vmContainer.homeVM.handle(action = uiEvent)
            is DiscoverViewAction -> vmContainer.discoverVM.handle(action = uiEvent)
            is ThreadViewAction -> vmContainer.threadVM.handle(action = uiEvent)
            is TopicViewAction -> vmContainer.topicVM.handle(action = uiEvent)
            is ProfileViewAction -> vmContainer.profileVM.handle(action = uiEvent)
            is SettingsViewAction -> vmContainer.settingsVM.handle(action = uiEvent)
            is CreatePostViewAction -> vmContainer.createPostVM.handle(action = uiEvent)
            is CreateGitIssueViewAction -> vmContainer.createGitIssueVM.handle(action = uiEvent)
            is CreateReplyViewAction -> vmContainer.createReplyVM.handle(action = uiEvent)
            is CreateCrossPostViewAction -> vmContainer.createCrossPostVM.handle(action = uiEvent)
            is SearchViewAction -> vmContainer.searchVM.handle(action = uiEvent)
            is EditProfileViewAction -> vmContainer.editProfileVM.handle(action = uiEvent)
            is RelayEditorViewAction -> vmContainer.relayEditorVM.handle(action = uiEvent)
            is InboxViewAction -> vmContainer.inboxVM.handle(action = uiEvent)
            is FollowListsViewAction -> vmContainer.followListsVM.handle(action = uiEvent)
            is BookmarksViewAction -> vmContainer.bookmarksVM.handle(action = uiEvent)
            is EditListViewAction -> vmContainer.editListVM.handle(action = uiEvent)
            is ListViewAction -> vmContainer.listVM.handle(action = uiEvent)
            is MuteListViewAction -> vmContainer.muteListVM.handle(action = uiEvent)

            is ProcessExternalSignature -> viewModelScope.launch {
                appContainer.externalSignerHandler.processExternalSignature(
                    result = uiEvent.activityResult
                )
            }

            is ClickClickableText -> clickText(action = uiEvent)

            is SuggestionAction -> appContainer.suggestionProvider.handle(action = uiEvent)

            is RegisterAccountLauncher -> appContainer.externalSignerHandler
                .setAccountLauncher(launcher = uiEvent.launcher)

            is RegisterSignerLauncher -> appContainer.externalSignerHandler
                .setSignerLauncher(launcher = uiEvent.launcher)

            is RegisterUriHandler -> appContainer.annotatedStringProvider
                .setUriHandler(uriHandler = uiEvent.uriHandler)


            is RebroadcastPost -> viewModelScope.launchIO {
                appContainer.eventRebroadcaster.rebroadcast(
                    postId = uiEvent.postId,
                    context = uiEvent.context,
                    uiScope = viewModelScope
                )
            }

            ClosePostInfo -> appContainer.postDetailInspector.closePostDetails()
            is OpenPostInfo -> viewModelScope.launchIO {
                appContainer.postDetailInspector.setPostDetails(postId = uiEvent.postId)
            }

            is OpenLightningWallet -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("lightning:${uiEvent.address}")
                )
                runCatching { uiEvent.launcher.launch(intent) }
                    .onFailure {
                        appContainer.snackbar.showToast(
                            scope = uiEvent.scope,
                            msg = appContainer.context.getString(R.string.you_dont_have_a_lightning_wallet_installed)
                        )
                    }
            }
        }
    }

    private fun clickText(action: ClickClickableText) {
        if (action.text.startsWith("#")) {
            onUpdate(OpenTopic(topic = action.text.normalizeTopic()))
            return
        }

        openNostrString(str = action.text, uriHandler = action.uriHandler)
    }

    private fun openNostrString(str: String, uriHandler: UriHandler? = null) {
        val onHandleUri = { nostrStr: String ->
            uriHandler?.openUri("https://njump.me/$nostrStr")
        }

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
                val nevent = Nip19Event.fromBech32(nostrMention.bech32)
                val kind = nevent.kind()
                if (kind == null || textNoteAndRepostKinds.contains(kind)) {
                    onUpdate(OpenThreadRaw(nevent = nevent))
                } else {
                    onHandleUri(nostrMention.bech32)
                }
            }

            is NoteMention -> {
                val nevent = createNevent(hex = nostrMention.hex)
                onUpdate(OpenThreadRaw(nevent = nevent))
            }

            is CoordinateMention -> {
                onHandleUri(nostrMention.bech32)
            }

            is RelayMention -> {
                val relayUrl = nostrMention.relay
                onUpdate(OpenRelayProfile(relayUrl = relayUrl))
            }

            null -> Log.w(TAG, "Unknown clickable string $str")
        }
    }

    override fun onCleared() {
        super.onCleared()
        appContainer.nostrService.close()
    }
}
