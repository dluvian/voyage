package com.dluvian.voyage.model

import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.Topic
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.navigator.BookmarkNavView
import com.dluvian.voyage.navigator.CreateGitIssueNavView
import com.dluvian.voyage.navigator.CreatePostNavView
import com.dluvian.voyage.navigator.CrossPostNavView
import com.dluvian.voyage.navigator.DiscoverNavView
import com.dluvian.voyage.navigator.EditProfileNavView
import com.dluvian.voyage.navigator.FollowListsNavView
import com.dluvian.voyage.navigator.HomeNavView
import com.dluvian.voyage.navigator.InboxNavView
import com.dluvian.voyage.navigator.NavView
import com.dluvian.voyage.navigator.ProfileNavView
import com.dluvian.voyage.navigator.RelayEditorNavView
import com.dluvian.voyage.navigator.RelayProfileNavView
import com.dluvian.voyage.navigator.ReplyNavView
import com.dluvian.voyage.navigator.SearchNavView
import com.dluvian.voyage.navigator.SettingsNavView
import com.dluvian.voyage.navigator.ThreadNavView
import com.dluvian.voyage.navigator.ThreadNeventNavView
import com.dluvian.voyage.navigator.TopicNavView
import kotlinx.coroutines.CoroutineScope
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.Nip19Event
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata

sealed class Cmd

sealed class NavCmd : Cmd()

sealed class PopNavCmd : NavCmd()
data object SystemBackPress : PopNavCmd()
data object GoBack : PopNavCmd()

sealed class PushNavCmd : NavCmd() {
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
            ClickBookmarks -> BookmarkNavView
            ClickCreateGitIssue -> CreateGitIssueNavView
            is OpenThread -> ThreadNavView(this.event)
            is OpenThreadNevent -> ThreadNeventNavView(nevent = this.nevent)
            is OpenProfile -> ProfileNavView(this.nprofile)
            is OpenTopic -> TopicNavView(topic = this.topic)
            is OpenReplyCreation -> ReplyNavView(parent = this.parent)
            is OpenCrossPostCreation -> CrossPostNavView(event = this.event)
            is OpenRelayProfile -> RelayProfileNavView(relayUrl = this.relayUrl)
        }
    }
}

data object ClickHome : PushNavCmd()
data object ClickDiscover : PushNavCmd()
data object ClickInbox : PushNavCmd()
data object ClickCreate : PushNavCmd()
data object ClickSettings : PushNavCmd()
data object ClickSearch : PushNavCmd()
data object ClickEditProfile : PushNavCmd()
data object ClickRelayEditor : PushNavCmd()
data object ClickFollowLists : PushNavCmd()
data object ClickBookmarks : PushNavCmd()
data object ClickCreateGitIssue : PushNavCmd()

sealed class AdvancedPushNavCmd : PushNavCmd()
data class OpenThread(val event: UIEvent) : AdvancedPushNavCmd()
data class OpenThreadNevent(val nevent: Nip19Event) : AdvancedPushNavCmd()
data class OpenProfile(val nprofile: Nip19Profile) : AdvancedPushNavCmd()
data class OpenTopic(val topic: Topic) : AdvancedPushNavCmd()
data class OpenReplyCreation(val parent: UIEvent) : AdvancedPushNavCmd()
data class OpenCrossPostCreation(val event: Event) : AdvancedPushNavCmd()
data class OpenRelayProfile(val relayUrl: RelayUrl) : AdvancedPushNavCmd()

sealed class CoreActionCmd : Cmd()
data class ClickUpvote(val event: Event) : CoreActionCmd()
data class ClickNeutralizeVotes(val event: Event) : CoreActionCmd()
data class FollowTopic(val topic: Topic) : CoreActionCmd()
data class UnfollowTopic(val topic: Topic) : CoreActionCmd()
data class BookmarkPost(val id: EventId) : CoreActionCmd()
data class UnbookmarkPost(val id: EventId) : CoreActionCmd()
data class FollowProfile(val pubkey: PublicKey) : CoreActionCmd()
data class UnfollowProfile(val pubkey: PublicKey) : CoreActionCmd()
data class Rebroadcast(val event: Event) : CoreActionCmd()
data class DeletePost(val event: Event) : CoreActionCmd()
data class SendCrossPost(val topics: List<Topic>, val event: Event) : CoreActionCmd()
data class SendPost(val topics: List<Topic>, val subject: String, val content: String) :
    CoreActionCmd()
data class SendReply(val parent: Event, val content: String) : CoreActionCmd()
data class SendGitIssue(val type: GitIssueType, val header: String, val content: String) :
    CoreActionCmd()
data class PublishNip65(val relays: List<Pair<RelayUrl, RelayMetadata?>>) : CoreActionCmd()
data class PublishProfile(val metadata: Metadata) : CoreActionCmd()
data class ShowEventDetails(val event: Event) : CoreActionCmd()
data object CloseEventDetails : CoreActionCmd()
data class SearchTopicSuggestion(val topic: Topic) : CoreActionCmd()
data class SearchProfileSuggestion(val name: String) : CoreActionCmd()
data class ClickProfileSuggestion(val pubkey: PublicKey) : CoreActionCmd()

sealed class HomeViewCmd : Cmd()
data object HomeViewOpen : HomeViewCmd()
data object HomeViewRefresh : HomeViewCmd()
data object HomeViewNextPage : HomeViewCmd()
data object HomeViewOpenFilter : HomeViewCmd()
data object HomeViewDismissFilter : HomeViewCmd()
data class HomeViewApplyFilter(val setting: HomeFeedSetting) : HomeViewCmd()

sealed class ThreadViewCmd : Cmd()
data class ThreadViewPushNevent(val nevent: Nip19Event) : ThreadViewCmd()
data class ThreadViewPushUIEvent(val uiEvent: UIEvent) : ThreadViewCmd()
data class ThreadViewPopNevent(val nevent: Nip19Event) : ThreadViewCmd()
data class ThreadViewPopUIEvent(val uiEvent: UIEvent) : ThreadViewCmd()
data object ThreadViewRefresh : ThreadViewCmd()
data class ThreadViewToggleCollapse(val id: EventId) : ThreadViewCmd()
data class ThreadViewShowReplies(val id: EventId) : ThreadViewCmd()

sealed class InboxViewCmd : Cmd()
data object InboxViewOpen : InboxViewCmd()
data object InboxViewRefresh : InboxViewCmd()
data object InboxViewAppend : InboxViewCmd()
data object InboxViewOpenFilter : InboxViewCmd()
data object InboxViewDismissFilter : InboxViewCmd()
data class InboxViewApplyFilter(val setting: InboxFeedSetting) : InboxViewCmd()

sealed class DiscoverViewCmd : Cmd()
data object DiscoverViewOpen : DiscoverViewCmd()
data object DiscoverViewRefresh : DiscoverViewCmd()

sealed class FollowListsViewCmd : Cmd()
data object FollowListsViewOpen : FollowListsViewCmd()
data object FollowListsViewRefresh : FollowListsViewCmd()

sealed class BookmarkViewCmd : Cmd()
data object BookmarkViewOpen : BookmarkViewCmd()
data object BookmarkViewRefresh : BookmarkViewCmd()
data object BookmarkViewNextPage : BookmarkViewCmd()

sealed class DrawerViewCmd : Cmd()
data class OpenDrawer(val scope: CoroutineScope) : DrawerViewCmd()
data class CloseDrawer(val scope: CoroutineScope) : DrawerViewCmd()

sealed class TopicViewCmd : Cmd()
data class TopicViewPush(val topic: Topic) : TopicViewCmd()
data class TopicViewPop(val topic: Topic) : TopicViewCmd()
data object TopicViewRefresh : TopicViewCmd()
data object TopicViewNextPage : TopicViewCmd()

sealed class RelayEditorViewCmd : Cmd()
data object RelayEditorViewOpen : RelayEditorViewCmd()
data class AddRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data class RemoveRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data class ToggleReadRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data class ToggleWriteRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data object RelayConnectionUpdated : RelayEditorViewCmd()

sealed class RelayProfileViewCmd : Cmd()
data class RelayProfileViewOpen(val relayUrl: RelayUrl) : RelayProfileViewCmd()

sealed class ProfileViewCmd : Cmd()
data class ProfileViewPushNprofile(val nprofile: Nip19Profile) : ProfileViewCmd()
data class ProfileViewPopNprofile(val nprofile: Nip19Profile) : ProfileViewCmd()
data object ProfileViewRefresh : ProfileViewCmd()
data object ProfileViewNextPage : ProfileViewCmd()

sealed class PostViewCmd : Cmd()
data object PostViewOpen : PostViewCmd()

sealed class GitIssueCmd : Cmd()
data object GitIssueOpen : GitIssueCmd()

sealed class ReplyViewCmd : Cmd()
data class ReplyViewOpen(val uiEvent: UIEvent) : ReplyViewCmd()

sealed class CrossPostViewCmd : Cmd()
data class CrossPostViewOpen(val event: Event) : CrossPostViewCmd()

sealed class EditProfileViewCmd : Cmd()
data object EditProfileViewOpen : EditProfileViewCmd()

sealed class SettingsViewCmd : Cmd()
data object SettingsViewOpen : SettingsViewCmd()
data object LoadSeed : SettingsViewCmd()
data class SendAuth(val sendAuth: Boolean) : SettingsViewCmd()
data class AddClientTag(val addClientTag: Boolean) : SettingsViewCmd()
data class ChangeUpvoteContent(val newContent: String) : SettingsViewCmd()
data class SwitchSigner(val signerType: SignerType) : SettingsViewCmd()

sealed class SearchViewCmd : Cmd()
data object SearchViewOpen : SearchViewCmd()
data class SearchText(val text: String) : SearchViewCmd()
data class UpdateSearchText(val text: String) : SearchViewCmd()

sealed class RelayNotificationCmd : Cmd()
data class ReceiveEvent(val event: Event) : RelayNotificationCmd()
data class RelayClosed(val relay: RelayUrl, val msg: String) : RelayNotificationCmd()
data class RelayNotice(val relay: RelayUrl, val msg: String) : RelayNotificationCmd()
