package com.dluvian.voyage.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.Topic
import rust.nostr.sdk.Event
import rust.nostr.sdk.Nip19Event
import rust.nostr.sdk.Nip19Profile

sealed class NavView


sealed class MainNavView : NavView() {
    @Composable
    @Stable
    fun getTitle(): String {
        return when (this) {
            is HomeNavView -> stringResource(id = R.string.home)
            is InboxNavView -> stringResource(id = R.string.inbox)
            is DiscoverNavView -> stringResource(id = R.string.discover)
            is SearchNavView -> stringResource(id = R.string.search)
        }
    }
}

data object HomeNavView : MainNavView()
data object InboxNavView : MainNavView()
data object SearchNavView : MainNavView()
data object DiscoverNavView : MainNavView()

sealed class NonMainNavView : NavView()
sealed class SimpleNonMainNavView : NonMainNavView()
data object CreatePostNavView : SimpleNonMainNavView()
data object SettingsNavView : SimpleNonMainNavView()
data object EditProfileNavView : SimpleNonMainNavView()
data object RelayEditorNavView : SimpleNonMainNavView()
data object FollowListsNavView : SimpleNonMainNavView()
data object BookmarkNavView : SimpleNonMainNavView()
data object CreateGitIssueNavView : SimpleNonMainNavView()

sealed class AdvancedNonMainNavView : NonMainNavView()
data class ThreadNavView(val event: Event) : AdvancedNonMainNavView()
data class ThreadNeventNavView(val nevent: Nip19Event) : AdvancedNonMainNavView()
data class ProfileNavView(val profileEvent: Event) : AdvancedNonMainNavView()
data class NProfileNavView(val nprofile: Nip19Profile) : AdvancedNonMainNavView()
data class TopicNavView(val topic: Topic) : AdvancedNonMainNavView()
data class ReplyNavView(val parent: Event) : AdvancedNonMainNavView()
data class CrossPostNavView(val event: Event) : AdvancedNonMainNavView()
data class RelayProfileNavView(val relayUrl: RelayUrl) : AdvancedNonMainNavView()
data class OpenListNavView(val identifier: String) : AdvancedNonMainNavView()
data object EditNewListNavView : AdvancedNonMainNavView()
data class EditExistingListNavView(val identifier: String) : AdvancedNonMainNavView()
