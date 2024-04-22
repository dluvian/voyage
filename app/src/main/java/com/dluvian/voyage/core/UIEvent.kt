package com.dluvian.voyage.core

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.RelayEditorNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import rust.nostr.protocol.Metadata
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
            is OpenThread -> ThreadNavView(rootPost = this.rootPost)
            is OpenProfile -> ProfileNavView(nprofile = this.nprofile)
            is OpenTopic -> TopicNavView(topic = this.topic)
            is OpenReplyCreation -> ReplyCreationNavView(parent = this.parent)
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


sealed class AdvancedPushNavEvent : PushNavEvent()
data class OpenThread(val rootPost: RootPostUI) : AdvancedPushNavEvent()
data class OpenProfile(val nprofile: Nip19Profile) : AdvancedPushNavEvent()
data class OpenTopic(val topic: Topic) : AdvancedPushNavEvent()
data class OpenReplyCreation(val parent: IParentUI) : AdvancedPushNavEvent()


sealed class VoteEvent(
    open val postId: EventIdHex,
    open val mention: PubkeyHex,
    open val signerLauncher: SignerLauncher
) : UIEvent()

data class ClickUpvote(
    override val postId: EventIdHex,
    override val mention: PubkeyHex,
    override val signerLauncher: SignerLauncher,
) : VoteEvent(postId = postId, mention = mention, signerLauncher = signerLauncher)

data class ClickDownvote(
    override val postId: EventIdHex,
    override val mention: PubkeyHex,
    override val signerLauncher: SignerLauncher,
) : VoteEvent(postId = postId, mention = mention, signerLauncher = signerLauncher)

data class ClickNeutralizeVote(
    override val postId: EventIdHex,
    override val mention: PubkeyHex,
    override val signerLauncher: SignerLauncher,
) : VoteEvent(postId = postId, mention = mention, signerLauncher = signerLauncher)


sealed class TopicEvent(open val topic: Topic, open val signerLauncher: SignerLauncher) : UIEvent()
data class FollowTopic(override val topic: Topic, override val signerLauncher: SignerLauncher) :
    TopicEvent(topic = topic, signerLauncher = signerLauncher)

data class UnfollowTopic(override val topic: Topic, override val signerLauncher: SignerLauncher) :
    TopicEvent(topic = topic, signerLauncher = signerLauncher)


sealed class ProfileEvent(open val pubkey: PubkeyHex, open val signerLauncher: SignerLauncher) :
    UIEvent()

data class FollowProfile(
    override val pubkey: PubkeyHex,
    override val signerLauncher: SignerLauncher
) :
    ProfileEvent(pubkey = pubkey, signerLauncher = signerLauncher)

data class UnfollowProfile(
    override val pubkey: PubkeyHex,
    override val signerLauncher: SignerLauncher
) :
    ProfileEvent(pubkey = pubkey, signerLauncher = signerLauncher)


sealed class PostEvent : UIEvent()
data class DeletePost(val id: EventIdHex, val signerLauncher: SignerLauncher) : PostEvent()


sealed class HomeViewAction : UIEvent()
data object HomeViewRefresh : HomeViewAction()
data object HomeViewAppend : HomeViewAction()
data object HomeViewSubAccountAndTrustData : HomeViewAction()


sealed class ThreadViewAction : UIEvent()
data object ThreadViewRefresh : ThreadViewAction()
data class ThreadViewToggleCollapse(val id: EventIdHex) : ThreadViewAction()
data class ThreadViewShowReplies(val id: EventIdHex) : ThreadViewAction()


sealed class DiscoverViewAction : UIEvent()
data object DiscoverViewInit : DiscoverViewAction()
data object DiscoverViewRefresh : DiscoverViewAction()


sealed class TopicViewAction : UIEvent()
data object TopicViewRefresh : TopicViewAction()
data object TopicViewAppend : TopicViewAction()


sealed class RelayEditorViewAction : UIEvent()
data class AddRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class RemoveRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data object LoadRelays : RelayEditorViewAction()
data object SaveRelays : RelayEditorViewAction()


sealed class ProfileViewAction : UIEvent()
data object ProfileViewRefresh : ProfileViewAction()
data object ProfileViewAppend : ProfileViewAction()


sealed class CreatePostViewAction : UIEvent()
data class SendPost(
    val header: String,
    val body: String,
    val topics: List<Topic>,
    val context: Context,
    val signerLauncher: SignerLauncher,
    val onGoBack: Fn
) : CreatePostViewAction()


sealed class CreateReplyViewAction : UIEvent()
data class SendReply(
    val parent: IParentUI,
    val body: String,
    val context: Context,
    val signerLauncher: SignerLauncher,
    val onGoBack: Fn
) : CreateReplyViewAction()


sealed class EditProfileViewAction : UIEvent()
data object LoadFullProfile : EditProfileViewAction()
data class SaveProfile(
    val metadata: Metadata,
    val signerLauncher: SignerLauncher,
    val context: Context,
    val onGoBack: Fn,
) : EditProfileViewAction()


sealed class SettingsViewAction : UIEvent()
data object UseDefaultAccount : SettingsViewAction()
data class RequestExternalAccount(
    val reqAccountLauncher: SignerLauncher,
    val context: Context
) : SettingsViewAction()
data class ProcessExternalAccount(
    val activityResult: ActivityResult,
    val context: Context
) : SettingsViewAction()
data class UpdateRootPostThreshold(val threshold: Float) : SettingsViewAction()
data object LoadSeed : SettingsViewAction()


sealed class SearchViewAction : UIEvent()
data class UpdateSearchText(val text: String) : SearchViewAction()
data class SearchText(
    val text: String,
    val context: Context,
    val onOpenTopic: (Topic) -> Unit,
    val onOpenProfile: (Nip19Profile) -> Unit
) : SearchViewAction()


data class ProcessExternalSignature(val activityResult: ActivityResult) : UIEvent()
data class ClickText(
    val text: AnnotatedString,
    val offset: Int,
    val uriHandler: UriHandler,
    val onNoneClick: Fn = {},
) : UIEvent()
