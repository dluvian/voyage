package com.dluvian.voyage.core

import android.content.Context
import androidx.activity.result.ActivityResult
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.navigator.CommentCreationNavView
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import rust.nostr.protocol.Nip19Profile

sealed class UIEvent

sealed class NavEvent : UIEvent()


sealed class PopNavEvent : NavEvent()
data object SystemBackPress : PopNavEvent()
data object GoBack : PopNavEvent()


sealed class PushNavEvent : NavEvent() {
    fun getNavView(): NavView {
        return when (this) {
            is ClickHome -> HomeNavView
            is ClickDiscover -> DiscoverNavView
            is ClickCreate -> CreatePostNavView
            is ClickInbox -> InboxNavView
            is ClickSettings -> SettingsNavView
            is ClickSearch -> SearchNavView
            is ClickEditProfile -> EditProfileNavView
            is OpenThread -> ThreadNavView(rootPost = this.rootPost)
            is OpenProfile -> ProfileNavView(nprofile = this.nprofile)
            is OpenTopic -> TopicNavView(topic = this.topic)
            is OpenCommentCreation -> CommentCreationNavView(rootPost = this.rootPost)
            is OpenReplyCreation -> ReplyCreationNavView(comment = this.comment)
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


sealed class AdvancedPushNavEvent : PushNavEvent()
data class OpenThread(val rootPost: RootPostUI) : AdvancedPushNavEvent()
data class OpenProfile(val nprofile: Nip19Profile) : AdvancedPushNavEvent()
data class OpenTopic(val topic: Topic) : AdvancedPushNavEvent()
data class OpenReplyCreation(val comment: CommentUI) : AdvancedPushNavEvent()
data class OpenCommentCreation(val rootPost: RootPostUI) : AdvancedPushNavEvent()


sealed class VoteEvent(open val postId: EventIdHex, open val pubkey: PubkeyHex) : UIEvent()
data class ClickUpvote(
    override val postId: EventIdHex,
    override val pubkey: PubkeyHex
) : VoteEvent(postId = postId, pubkey = pubkey)

data class ClickDownvote(
    override val postId: EventIdHex,
    override val pubkey: PubkeyHex
) : VoteEvent(postId = postId, pubkey = pubkey)

data class ClickNeutralizeVote(
    override val postId: EventIdHex,
    override val pubkey: PubkeyHex
) : VoteEvent(postId = postId, pubkey = pubkey)


sealed class TopicEvent(open val topic: Topic) : UIEvent()
data class FollowTopic(override val topic: Topic) : TopicEvent(topic = topic)
data class UnfollowTopic(override val topic: Topic) : TopicEvent(topic = topic)


sealed class ProfileEvent(open val pubkey: PubkeyHex) : UIEvent()
data class FollowProfile(override val pubkey: PubkeyHex) : ProfileEvent(pubkey = pubkey)
data class UnfollowProfile(override val pubkey: PubkeyHex) : ProfileEvent(pubkey = pubkey)


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


sealed class ProfileViewAction : UIEvent()
data object ProfileViewRefresh : ProfileViewAction()
data object ProfileViewAppend : ProfileViewAction()


sealed class CreatePostViewAction : UIEvent()
data class SendPost(
    val header: String,
    val body: String,
    val context: Context,
    val onGoBack: Fn
) : CreatePostViewAction()


sealed class CreateResponseViewAction : UIEvent()
data class SendResponse(
    val body: String,
    val context: Context,
    val onGoBack: Fn
) : CreateResponseViewAction()


sealed class SettingsViewAction : UIEvent()
data object UseDefaultAccount : SettingsViewAction()
data class RequestExternalAccount(val context: Context) : SettingsViewAction()
data class ProcessExternalAccount(
    val activityResult: ActivityResult,
    val context: Context
) : SettingsViewAction()


sealed class SearchViewAction : UIEvent()
data class UpdateSearchText(val text: String) : SearchViewAction()
data class SearchText(
    val text: String,
    val context: Context,
    val onOpenTopic: (Topic) -> Unit,
    val onOpenProfile: (Nip19Profile) -> Unit
) : SearchViewAction()


data class ProcessExternalSignature(val activityResult: ActivityResult) : UIEvent()
