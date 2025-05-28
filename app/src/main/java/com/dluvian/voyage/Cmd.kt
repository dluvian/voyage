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
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.OpenListNavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.RelayEditorNavView
import com.dluvian.voyage.core.navigator.RelayProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.ThreadRawNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.model.LabledGitIssue
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
            is OpenThread -> ThreadNavView(event = this.event)
            is OpenProfile -> ProfileNavView(nprofile = this.nprofile)
            is OpenTopic -> TopicNavView(topic = this.topic)
            is OpenReplyCreation -> ReplyCreationNavView(parent = this.parent)
            is OpenThreadRaw -> ThreadRawNavView(nevent = this.nevent, parent = this.parent)
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
data class OpenThreadRaw(val nevent: Nip19Event, val parent: Event? = null) : AdvancedPushNavCmd()
data class OpenProfile(val nprofile: Nip19Profile) : AdvancedPushNavCmd()
data class OpenTopic(val topic: Topic) : AdvancedPushNavCmd()
data class OpenReplyCreation(val parent: Event) : AdvancedPushNavCmd()
data class OpenCrossPostCreation(val event: Event) : AdvancedPushNavCmd()
data class OpenRelayProfile(val relayUrl: RelayUrl) : AdvancedPushNavCmd()
data class OpenList(val identifier: Ident) : AdvancedPushNavCmd()
data class EditList(val identifier: Ident) : AdvancedPushNavCmd()

sealed class VoteCmd(open val event: Event) : Cmd()
data class ClickUpvote(override val event: Event) : VoteCmd(event = event)
data class ClickNeutralizeVote(override val event: Event) : VoteCmd(event = event)

sealed class TopicCmd(open val topic: Topic) : Cmd()
data class FollowTopic(override val topic: Topic) : TopicCmd(topic = topic)
data class UnfollowTopic(override val topic: Topic) : TopicCmd(topic = topic)

sealed class BookmarkCmd(open val event: Event) : Cmd()
data class BookmarkPost(override val event: Event) : BookmarkCmd(event = event)
data class UnbookmarkPost(override val event: Event) : BookmarkCmd(event = event)

sealed class ProfileCmd(open val pubkey: PublicKey) : Cmd()
data class FollowProfile(override val pubkey: PublicKey) : ProfileCmd(pubkey = pubkey)
data class UnfollowProfile(override val pubkey: PublicKey) : ProfileCmd(pubkey = pubkey)

sealed class HomeViewAction : Cmd()
data object HomeViewRefresh : HomeViewAction()
data object HomeViewAppend : HomeViewAction()
data object HomeViewSubAccountAndTrustData : HomeViewAction()
data object HomeViewOpenFilter : HomeViewAction()
data object HomeViewDismissFilter : HomeViewAction()
data class HomeViewApplyFilter(val setting: HomeFeedSetting) : HomeViewAction()

sealed class ThreadViewAction : Cmd()
data object ThreadViewRefresh : ThreadViewAction()
data class ThreadViewToggleCollapse(val id: EventId) : ThreadViewAction()
data class ThreadViewShowReplies(val id: EventId) : ThreadViewAction()

sealed class InboxViewAction : Cmd()
data object InboxViewInit : InboxViewAction()
data object InboxViewRefresh : InboxViewAction()
data object InboxViewAppend : InboxViewAction()
data object InboxViewOpenFilter : InboxViewAction()
data object InboxViewDismissFilter : InboxViewAction()
data class InboxViewApplyFilter(val setting: InboxFeedSetting) : InboxViewAction()

sealed class DiscoverViewAction : Cmd()
data object DiscoverViewInit : DiscoverViewAction()
data object DiscoverViewRefresh : DiscoverViewAction()

sealed class FollowListsViewAction : Cmd()
data object FollowListsViewInit : FollowListsViewAction()
data object FollowListsViewRefresh : FollowListsViewAction()

sealed class BookmarksViewAction : Cmd()
data object BookmarksViewInit : BookmarksViewAction()
data object BookmarksViewRefresh : BookmarksViewAction()
data object BookmarksViewAppend : BookmarksViewAction()

sealed class EditListViewAction : Cmd()
data class EditListViewSave(val context: Context, val onGoBack: () -> Unit) : EditListViewAction()
data class EditListViewAddProfile(val profile: Pair<PublicKey, Metadata>) : EditListViewAction()
data class EditListViewAddTopic(val topic: Topic) : EditListViewAction()

data class AddItemToList(
    val identifier: String,
    val scope: CoroutineScope,
    val context: Context
) : Cmd()

sealed class ListViewAction : Cmd()
data object ListViewRefresh : ListViewAction()
data object ListViewFeedAppend : ListViewAction()

sealed class DrawerViewAction : Cmd()
data object DrawerViewSubscribeSets : DrawerViewAction()
data class OpenDrawer(val scope: CoroutineScope) : DrawerViewAction()
data class CloseDrawer(val scope: CoroutineScope) : DrawerViewAction()

sealed class TopicViewAction : Cmd()
data object TopicViewRefresh : TopicViewAction()
data object TopicViewAppend : TopicViewAction()
data object TopicViewLoadLists : TopicViewAction()

sealed class RelayEditorViewAction : Cmd()
data class AddRelay(
    val relayUrl: RelayUrl,
    val scope: CoroutineScope,
    val context: Context
) : RelayEditorViewAction()

data class RemoveRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class ToggleReadRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class ToggleWriteRelay(val relayUrl: RelayUrl) : RelayEditorViewAction()
data class SaveRelays(val context: Context, val onGoBack: () -> Unit) : RelayEditorViewAction()
data object LoadRelays : RelayEditorViewAction()

sealed class ProfileViewAction : Cmd()
data object ProfileViewRefresh : ProfileViewAction()
data object ProfileViewRootAppend : ProfileViewAction()
data object ProfileViewLoadLists : ProfileViewAction()

sealed class CreatePostViewAction : Cmd()
data class SendPost(
    val header: String,
    val body: String,
    val topics: List<Topic>,
    val context: Context,
    val onGoBack: () -> Unit
) : CreatePostViewAction()

sealed class CreateGitIssueViewAction : Cmd()
data class SendGitIssue(
    val issue: LabledGitIssue,
    val context: Context,
    val onGoBack: () -> Unit
) : CreateGitIssueViewAction()
data object SubRepoOwnerRelays : CreateGitIssueViewAction()

sealed class CreateReplyViewAction : Cmd()
data class SendReply(
    val parent: Event,
    val body: String,
    val context: Context,
    val onGoBack: () -> Unit
) : CreateReplyViewAction()

sealed class CreateCrossPostViewAction : Cmd()
data class SendCrossPost(
    val topics: List<Topic>,
    val context: Context,
    val onGoBack: () -> Unit
) : CreateCrossPostViewAction()

sealed class SuggestionAction : Cmd()
data class SearchProfileSuggestion(val name: String) : SuggestionAction()
data class ClickProfileSuggestion(val profileEvent: Event) : SuggestionAction()
data class SearchTopicSuggestion(val topic: Topic) : SuggestionAction()

sealed class EditProfileViewAction : Cmd()
data object LoadFullProfile : EditProfileViewAction()
data class SaveProfile(
    val metadata: Metadata,
    val context: Context,
    val onGoBack: () -> Unit,
) : EditProfileViewAction()

sealed class SettingsViewAction : Cmd()
data object LoadSeed : SettingsViewAction()
data class SendAuth(val sendAuth: Boolean) : SettingsViewAction()
data class AddClientTag(val addClientTag: Boolean) : SettingsViewAction()
data class ExportDatabase(val uiScope: CoroutineScope) : SettingsViewAction()
data class ResetDatabase(val uiScope: CoroutineScope) : SettingsViewAction()
data class ChangeUpvoteContent(val newContent: String) : SettingsViewAction()

sealed class SearchViewAction : Cmd()
data object SubUnknownProfiles : SearchViewAction()
data class UpdateSearchText(val text: String) : SearchViewAction()
data class SearchText(
    val text: String,
    val context: Context,
    val onUpdate: (Cmd) -> Unit
) : SearchViewAction()

data class ClickClickableText(val text: String, val uriHandler: UriHandler) : Cmd()

data class RegisterUriHandler(val uriHandler: UriHandler) : Cmd()
data class Rebroadcast(val event: Event, val context: Context) : Cmd()
data class DeleteList(val identifier: Ident, val onCloseDrawer: () -> Unit) : Cmd()
data class DeletePost(val event: Event) : Cmd()
