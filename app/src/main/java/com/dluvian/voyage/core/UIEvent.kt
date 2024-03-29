package com.dluvian.voyage.core

import android.content.Context
import androidx.activity.result.ActivityResult
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.NavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.TopicNavView
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
            is ClickHome -> HomeNavView
            is ClickDiscover -> DiscoverNavView
            is ClickCreate -> CreatePostNavView
            is ClickInbox -> InboxNavView
            is ClickSettings -> SettingsNavView
            is ClickSearch -> SearchNavView
            is OpenThread -> ThreadNavView(nevent = this.nevent)
            is OpenProfile -> ProfileNavView(nprofile = this.nprofile)
            is OpenTopic -> TopicNavView(topic = this.topic)
        }
    }
}

data object ClickHome : PushNavEvent()
data object ClickDiscover : PushNavEvent()
data object ClickInbox : PushNavEvent()
data object ClickCreate : PushNavEvent()
data object ClickSettings : PushNavEvent()
data object ClickSearch : PushNavEvent()

sealed class AdvancedPushNavEvent : PushNavEvent()
data class OpenThread(val nevent: Nip19Event) : AdvancedPushNavEvent()
data class OpenProfile(val nprofile: Nip19Profile) : AdvancedPushNavEvent()
data class OpenTopic(val topic: Topic) : AdvancedPushNavEvent()


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
data class DiscoverViewFollowTopic(val topic: Topic) : DiscoverViewAction()
data class DiscoverViewUnfollowTopic(val topic: Topic) : DiscoverViewAction()
data class DiscoverViewFollowProfile(val pubkey: PubkeyHex) : DiscoverViewAction()
data class DiscoverViewUnfollowProfile(val pubkey: PubkeyHex) : DiscoverViewAction()


sealed class TopicViewAction : UIEvent()
data object TopicViewRefresh : TopicViewAction()
data object TopicViewAppend : TopicViewAction()
data class TopicViewFollowTopic(val topic: Topic) : TopicViewAction()
data class TopicViewUnfollowTopic(val topic: Topic) : TopicViewAction()


sealed class ProfileViewAction : UIEvent()
data object ProfileViewRefresh : ProfileViewAction()
data object ProfileViewAppend : ProfileViewAction()
data class ProfileViewFollowProfile(val pubkey: PubkeyHex) : ProfileViewAction()
data class ProfileViewUnfollowProfile(val pubkey: PubkeyHex) : ProfileViewAction()


sealed class CreatePostViewAction : UIEvent()
data class CreatePostViewSendPost(
    val header: String,
    val body: String,
    val context: Context,
    val onGoBack: Fn
) : CreatePostViewAction()


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
