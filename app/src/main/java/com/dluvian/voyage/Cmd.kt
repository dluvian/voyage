package com.dluvian.voyage

import android.content.Context
import androidx.compose.ui.platform.UriHandler
import com.dluvian.voyage.core.navigator.BookmarksNavView
import com.dluvian.voyage.core.navigator.CreateGitIssueNavView
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.CrossPostCreationNavView
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.EditExistingListNavView
import com.dluvian.voyage.core.navigator.EditNewListNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.FollowListsNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.NProfileNavView
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.OpenListNavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.RelayEditorNavView
import com.dluvian.voyage.core.navigator.RelayProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.ThreadNeventNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import kotlinx.coroutines.CoroutineScope
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.Nip19Event
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.PublicKey

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
            ClickBookmarks -> BookmarksNavView
            ClickCreateList -> EditNewListNavView
            ClickCreateGitIssue -> CreateGitIssueNavView
            is OpenThread -> ThreadNavView(this.event)
            is OpenThreadLink -> ThreadNeventNavView(nevent = this.nevent)
            is OpenNProfile -> NProfileNavView(this.nprofile)
            is OpenProfile -> ProfileNavView(this.profileEvent)
            is OpenTopic -> TopicNavView(topic = this.topic)
            is OpenReplyCreation -> ReplyCreationNavView(parent = this.parent)
            is OpenCrossPostCreation -> CrossPostCreationNavView(event = this.event)
            is OpenRelayProfile -> RelayProfileNavView(relayUrl = this.relayUrl)
            is OpenList -> OpenListNavView(identifier = this.identifier)
            is EditList -> EditExistingListNavView(identifier = this.identifier)
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
data object ClickCreateList : PushNavCmd()
data object ClickCreateGitIssue : PushNavCmd()

sealed class AdvancedPushNavCmd : PushNavCmd()
data class OpenThread(val event: Event) : AdvancedPushNavCmd()
data class OpenThreadLink(val nevent: Nip19Event) : AdvancedPushNavCmd()
data class OpenProfile(val profileEvent: Event) : AdvancedPushNavCmd()
data class OpenNProfile(val nprofile: Nip19Profile) : AdvancedPushNavCmd()
data class OpenTopic(val topic: Topic) : AdvancedPushNavCmd()
data class OpenReplyCreation(val parent: Event) : AdvancedPushNavCmd()
data class OpenCrossPostCreation(val event: Event) : AdvancedPushNavCmd()
data class OpenRelayProfile(val relayUrl: RelayUrl) : AdvancedPushNavCmd()
data class OpenList(val identifier: Ident) : AdvancedPushNavCmd()
data class EditList(val identifier: Ident) : AdvancedPushNavCmd()

data class ClickUpvote(val event: Event) : Cmd()
data class ClickNeutralizeVotes(val event: Event) : Cmd()

data class FollowTopic(val topic: Topic) : Cmd()
data class UnfollowTopic(val topic: Topic) : Cmd()

data class BookmarkPost(val event: Event) : Cmd()
data class UnbookmarkPost(val event: Event) : Cmd()

data class FollowProfile(val pubkey: PublicKey) : Cmd()
data class UnfollowProfile(val pubkey: PublicKey) : Cmd()

data class AddPubkeyToList(val pubkey: PublicKey, val ident: String) : Cmd()
data class AddTopicToList(val topic: Topic, val ident: String) : Cmd()
data class ClickClickableText(val text: String, val uriHandler: UriHandler) : Cmd()
data class RegisterUriHandler(val uriHandler: UriHandler) : Cmd()
data class Rebroadcast(val event: Event) : Cmd()
data class DeleteList(val ident: Ident) : Cmd()
data class DeletePost(val event: Event) : Cmd()

sealed class HomeViewCmd : Cmd()
data object HomeViewRefresh : HomeViewCmd()
data object HomeViewAppend : HomeViewCmd()
data object HomeViewSubAccountAndTrustData : HomeViewCmd()
data object HomeViewOpenFilter : HomeViewCmd()
data object HomeViewDismissFilter : HomeViewCmd()
data class HomeViewApplyFilter(val setting: HomeFeedSetting) : HomeViewCmd()

sealed class ThreadViewCmd : Cmd()
data object ThreadViewRefresh : ThreadViewCmd()
data class ThreadViewToggleCollapse(val id: EventId) : ThreadViewCmd()
data class ThreadViewShowReplies(val id: EventId) : ThreadViewCmd()

sealed class InboxViewCmd : Cmd()
data object InboxViewInit : InboxViewCmd()
data object InboxViewRefresh : InboxViewCmd()
data object InboxViewAppend : InboxViewCmd()
data object InboxViewOpenFilter : InboxViewCmd()
data object InboxViewDismissFilter : InboxViewCmd()
data class InboxViewApplyFilter(val setting: InboxFeedSetting) : InboxViewCmd()

sealed class DiscoverViewCmd : Cmd()
data object DiscoverViewInit : DiscoverViewCmd()
data object DiscoverViewRefresh : DiscoverViewCmd()

sealed class FollowListsViewCmd : Cmd()
data object FollowListsViewInit : FollowListsViewCmd()
data object FollowListsViewRefresh : FollowListsViewCmd()

sealed class BookmarksViewCmd : Cmd()
data object BookmarksViewInit : BookmarksViewCmd()
data object BookmarksViewRefresh : BookmarksViewCmd()
data object BookmarksViewAppend : BookmarksViewCmd()

sealed class EditListViewCmd : Cmd()
data class EditListViewSave(val context: Context, val onGoBack: () -> Unit) : EditListViewCmd()
data class EditListViewAddProfile(val profile: Pair<PublicKey, Metadata>) : EditListViewCmd()
data class EditListViewAddTopic(val topic: Topic) : EditListViewCmd()

sealed class ListViewCmd : Cmd()
data object ListViewRefresh : ListViewCmd()
data object ListViewFeedAppend : ListViewCmd()

sealed class DrawerViewCmd : Cmd()
data object DrawerViewSubscribeSets : DrawerViewCmd()
data class OpenDrawer(val scope: CoroutineScope) : DrawerViewCmd()
data class CloseDrawer(val scope: CoroutineScope) : DrawerViewCmd()

sealed class TopicViewCmd : Cmd()
data object TopicViewRefresh : TopicViewCmd()
data object TopicViewAppend : TopicViewCmd()
data object TopicViewLoadLists : TopicViewCmd()

sealed class RelayEditorViewCmd : Cmd()
data class AddRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data class RemoveRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data class ToggleReadRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data class ToggleWriteRelay(val relayUrl: RelayUrl) : RelayEditorViewCmd()
data object SaveRelays : RelayEditorViewCmd()
data object LoadRelays : RelayEditorViewCmd()

sealed class ProfileViewCmd : Cmd()
data object ProfileViewRefresh : ProfileViewCmd()
data object ProfileViewRootAppend : ProfileViewCmd()
data object ProfileViewLoadLists : ProfileViewCmd()

sealed class CreatePostViewCmd : Cmd()
data class SendPost(
    val header: String,
    val body: String,
    val topics: List<Topic>,
) : CreatePostViewCmd()

sealed class CreateGitIssueViewCmd : Cmd()
data class SendGitIssue(val issue: String) : CreateGitIssueViewCmd()
data object SubRepoOwnerRelays : CreateGitIssueViewCmd()

sealed class CreateReplyViewCmd : Cmd()
data class SendReply(val parent: Event, val body: String) : CreateReplyViewCmd()

sealed class CreateCrossPostViewCmd : Cmd()
data class SendCrossPost(val topics: List<Topic>) : CreateCrossPostViewCmd()

sealed class SuggestionCmd : Cmd()
data class SearchProfileSuggestion(val name: String) : SuggestionCmd()
data class ClickProfileSuggestion(val profileEvent: Event) : SuggestionCmd()
data class SearchTopicSuggestion(val topic: Topic) : SuggestionCmd()

sealed class EditProfileViewCmd : Cmd()
data object LoadFullProfile : EditProfileViewCmd()
data class SaveProfile(val metadata: Metadata) : EditProfileViewCmd()

sealed class SettingsViewCmd : Cmd()
data object LoadSeed : SettingsViewCmd()
data class SendAuth(val sendAuth: Boolean) : SettingsViewCmd()
data class AddClientTag(val addClientTag: Boolean) : SettingsViewCmd()
data class ExportDatabase(val uiScope: CoroutineScope) : SettingsViewCmd()
data class ResetDatabase(val uiScope: CoroutineScope) : SettingsViewCmd()
data class ChangeUpvoteContent(val newContent: String) : SettingsViewCmd()

sealed class SearchViewCmd : Cmd()
data object SubUnknownProfiles : SearchViewCmd()
data class UpdateSearchText(val text: String) : SearchViewCmd()
data class SearchText(
    val text: String,
    val context: Context,
    val onUpdate: (Cmd) -> Unit
) : SearchViewCmd()

sealed class RelayNotificationCmd : Cmd()
data class ReceiveEvent(val event: Event) : RelayNotificationCmd()
data class RelayClosed(val relay: RelayUrl, val msg: String) : RelayNotificationCmd()
data class RelayNotice(val relay: RelayUrl, val msg: String) : RelayNotificationCmd()
