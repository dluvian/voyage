package com.dluvian.voyage.core

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.CrossPostCreationNavView
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.FollowListsNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.RelayEditorNavView
import com.dluvian.voyage.core.navigator.RelayProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.ThreadRawNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import kotlinx.coroutines.CoroutineScope
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile

sealed class UIEvent

sealed class NavEvent : UIEvent()


sealed class PopNavEvent : NavEvent()
data object SystemBackPress : PopNavEvent()
data object GoBack : PopNavEvent()


sealed class PushNavEvent : NavEvent() {
    fun getNavView(): NavView {
        return when (this) {
            ClickHome -> HomeNavView
            ClickDiscover -> DiscoverNavView
            ClickCreate -> CreatePostNavView
            ClickInbox -> InboxNavView
            ClickSettings -> SettingsNavView
            ClickSearch -> SearchNavView
            ClickEditProfile -> EditProfileNavView
            ClickRelayEditor -> RelayEditorNavView
            ClickFollowLists -> FollowListsNavView
            is OpenThread -> ThreadNavView(rootPost = this.rootPost)
            is OpenProfile -> ProfileNavView(nprofile = this.nprofile)
            is OpenTopic -> TopicNavView(topic = this.topic)
            is OpenReplyCreation -> ReplyCreationNavView(parent = this.parent)
            is OpenThreadRaw -> ThreadRawNavView(nevent = this.nevent, parent = this.parent)
            is OpenCrossPostCreation -> CrossPostCreationNavView(id = this.id)
            is OpenRelayProfile -> RelayProfileNavView(relayUrl = this.relayUrl)
        }
    }
}

data object ClickHome : PushNavEvent()
data object ClickDiscover : PushNavEvent()
data object ClickInbox : PushNavEvent()
data object ClickCreate : PushNavEvent()
data object ClickSettings : PushNavEvent()
data object ClickSearch : PushNavEvent()
data object ClickEditProfile : PushNavEvent()
data object ClickRelayEditor : PushNavEvent()
data object ClickFollowLists : PushNavEvent()


sealed class AdvancedPushNavEvent : PushNavEvent()
data class OpenThread(val rootPost: RootPostUI) : AdvancedPushNavEvent()
data class OpenThreadRaw(val nevent: Nip19Event, val parent: ParentUI? = null) :
    AdvancedPushNavEvent()
data class OpenProfile(val nprofile: Nip19Profile) : AdvancedPushNavEvent()
data class OpenTopic(val topic: Topic) : AdvancedPushNavEvent()
data class OpenReplyCreation(val parent: ParentUI) : AdvancedPushNavEvent()
data class OpenCrossPostCreation(val id: EventIdHex) : AdvancedPushNavEvent()
data class OpenRelayProfile(val relayUrl: RelayUrl) : AdvancedPushNavEvent()


sealed class VoteEvent(open val postId: EventIdHex, open val mention: PubkeyHex) : UIEvent()

data class ClickUpvote(
    override val postId: EventIdHex,
    override val mention: PubkeyHex,
) : VoteEvent(postId = postId, mention = mention)

data class ClickDownvote(
    override val postId: EventIdHex,
    override val mention: PubkeyHex,
) : VoteEvent(postId = postId, mention = mention)

data class ClickNeutralizeVote(
    override val postId: EventIdHex,
    override val mention: PubkeyHex,
) : VoteEvent(postId = postId, mention = mention)


sealed class TopicEvent(open val topic: Topic) : UIEvent()
data class FollowTopic(override val topic: Topic) : TopicEvent(topic = topic)

data class UnfollowTopic(override val topic: Topic) : TopicEvent(topic = topic)


sealed class ProfileEvent(open val pubkey: PubkeyHex) : UIEvent()

data class FollowProfile(override val pubkey: PubkeyHex) : ProfileEvent(pubkey = pubkey)

data class UnfollowProfile(override val pubkey: PubkeyHex) : ProfileEvent(pubkey = pubkey)


sealed class PostEvent : UIEvent()
data class DeletePost(val id: EventIdHex) : PostEvent()


sealed class HomeViewAction : UIEvent()
data object HomeViewRefresh : HomeViewAction()
data object HomeViewAppend : HomeViewAction()
data object HomeViewSubAccountAndTrustData : HomeViewAction()


sealed class ThreadViewAction : UIEvent()
data object ThreadViewRefresh : ThreadViewAction()
data class ThreadViewToggleCollapse(val id: EventIdHex) : ThreadViewAction()
data class ThreadViewShowReplies(val id: EventIdHex) : ThreadViewAction()


sealed class InboxViewAction : UIEvent()
data object InboxViewInit : InboxViewAction()
data object InboxViewRefresh : InboxViewAction()
data object InboxViewAppend : InboxViewAction()


sealed class DiscoverViewAction : UIEvent()
data object DiscoverViewInit : DiscoverViewAction()
data object DiscoverViewRefresh : DiscoverViewAction()


sealed class FollowListsViewAction : UIEvent()
data object FollowListsViewInit : FollowListsViewAction()
data object FollowListsViewRefresh : FollowListsViewAction()


sealed class DrawerAction : UIEvent()
data class OpenDrawer(val scope: CoroutineScope) : DrawerAction()
data class CloseDrawer(val scope: CoroutineScope) : DrawerAction()


sealed class TopicViewAction : UIEvent()
data object TopicViewRefresh : TopicViewAction()
data object TopicViewAppend : TopicViewAction()


sealed class RelayEditorViewAction : UIEvent()
data class AddRelay(
    val relayUrl: RelayUrl,
    val scope: CoroutineScope,
    val context: Context
) : RelayEditorViewAction()

data class RemoveRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class ToggleReadRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class ToggleWriteRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class SaveRelays(val context: Context, val onGoBack: Fn) : RelayEditorViewAction()

data object LoadRelays : RelayEditorViewAction()


sealed class ProfileViewAction : UIEvent()
data object ProfileViewRefresh : ProfileViewAction()
data object ProfileViewRootAppend : ProfileViewAction()
data object ProfileViewReplyAppend : ProfileViewAction()

sealed class CreatePostViewAction : UIEvent()
data class SendPost(
    val header: String,
    val body: String,
    val topics: List<Topic>,
    val context: Context,
    val onGoBack: Fn
) : CreatePostViewAction()

data object UpdatePostTopics : CreatePostViewAction()


sealed class CreateReplyViewAction : UIEvent()
data class SendReply(
    val parent: ParentUI,
    val body: String,
    val context: Context,
    val onGoBack: Fn
) : CreateReplyViewAction()


sealed class CreateCrossPostViewAction : UIEvent()
data class SendCrossPost(
    val topics: List<Topic>,
    val context: Context,
    val onGoBack: Fn
) : CreateCrossPostViewAction()

data object UpdateCrossPostTopics : CreateCrossPostViewAction()


sealed class ProfileSuggestionAction : UIEvent()
data class SearchSuggestion(val name: String) : ProfileSuggestionAction()
data object ClickSuggestion : ProfileSuggestionAction()


sealed class EditProfileViewAction : UIEvent()
data object LoadFullProfile : EditProfileViewAction()
data class SaveProfile(
    val metadata: Metadata,
    val context: Context,
    val onGoBack: Fn,
) : EditProfileViewAction()


sealed class SettingsViewAction : UIEvent()
data object UseDefaultAccount : SettingsViewAction()
data class RequestExternalAccount(val context: Context) : SettingsViewAction()

data class ProcessExternalAccount(
    val activityResult: ActivityResult,
    val context: Context
) : SettingsViewAction()

data class UpdateRootPostThreshold(val threshold: Float) : SettingsViewAction()
data object LoadSeed : SettingsViewAction()
data class SendAuth(val sendAuth: Boolean) : SettingsViewAction()


sealed class SearchViewAction : UIEvent()
data object SubUnknownProfiles : SearchViewAction()
data class UpdateSearchText(val text: String) : SearchViewAction()
data class SearchText(
    val text: String,
    val context: Context,
    val onUpdate: OnUpdate
) : SearchViewAction()


data class ProcessExternalSignature(val activityResult: ActivityResult) : UIEvent()
data class ClickText(
    val text: AnnotatedString,
    val offset: Int,
    val uriHandler: UriHandler,
    val onNoneClick: Fn = {},
) : UIEvent()

data class RegisterSignerLauncher(val launcher: SignerLauncher) : UIEvent()
data class RegisterAccountLauncher(val launcher: SignerLauncher) : UIEvent()

data class RebroadcastNote(val noteId: EventIdHex, val context: Context) : UIEvent()
