package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.model.RootPostUI
import rust.nostr.protocol.Nip19Profile

sealed class NavView


sealed class MainNavView : NavView() {
    @Composable
    @Stable
    fun getTitle(): String {
        return when (this) {
            is HomeNavView -> stringResource(id = R.string.home)
            is InboxNavView -> stringResource(id = R.string.inbox)
            is DiscoverNavView -> stringResource(id = R.string.discover)
        }
    }
}
data object HomeNavView : MainNavView()
data object DiscoverNavView : MainNavView()
data object InboxNavView : MainNavView()


sealed class NonMainNavView : NavView()
sealed class SimpleNonMainNavView : NonMainNavView()
data object CreatePostNavView : SimpleNonMainNavView()
data object SettingsNavView : SimpleNonMainNavView()
data object SearchNavView : SimpleNonMainNavView()
data object EditProfileNavView : SimpleNonMainNavView()
data object RelayEditorNavView : SimpleNonMainNavView()


sealed class AdvancedNonMainNavView : NonMainNavView()
data class ThreadNavView(val rootPost: RootPostUI) : AdvancedNonMainNavView()
data class ProfileNavView(val nprofile: Nip19Profile) : AdvancedNonMainNavView()
data class TopicNavView(val topic: Topic) : AdvancedNonMainNavView()
data class ReplyCreationNavView(val parent: IParentUI) : AdvancedNonMainNavView()
