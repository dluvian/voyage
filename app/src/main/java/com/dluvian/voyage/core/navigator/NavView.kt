package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.ThreadableMainEvent
import com.dluvian.voyage.data.nostr.RelayUrl
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
data object BookmarksNavView : SimpleNonMainNavView()
data object CreateGitIssueNavView : SimpleNonMainNavView()


sealed class AdvancedNonMainNavView : NonMainNavView()
data class ThreadNavView(val mainEvent: ThreadableMainEvent) : AdvancedNonMainNavView()
data class ThreadRawNavView(val nevent: Nip19Event, val parent: ThreadableMainEvent?) :
    AdvancedNonMainNavView()

data class ProfileNavView(val nprofile: Nip19Profile) : AdvancedNonMainNavView()
data class TopicNavView(val topic: Topic) : AdvancedNonMainNavView()
data class ReplyCreationNavView(val parent: MainEvent) : AdvancedNonMainNavView()
data class CrossPostCreationNavView(val id: EventIdHex) : AdvancedNonMainNavView()
data class RelayProfileNavView(val relayUrl: RelayUrl) : AdvancedNonMainNavView()
data class OpenListNavView(val identifier: String) : AdvancedNonMainNavView()
data object EditNewListNavView : AdvancedNonMainNavView()
data class EditExistingListNavView(val identifier: String) : AdvancedNonMainNavView()
