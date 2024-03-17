package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Topic
import rust.nostr.protocol.Nip19Event
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


sealed class NonMainNavView : NavView() {
    @Composable
    @Stable
    fun getTitle(): String? {
        return when (this) {
            is CreatePostNavView -> null
            SettingsNavView -> stringResource(id = R.string.settings)
            SearchNavView -> null
            is ProfileNavView -> this.nip19Profile.toBech32()
            is ThreadNavView -> this.nip19Event.eventId().toHex()
            is TopicNavView -> this.topic
        }
    }
}


sealed class SimpleNonMainNavView : NonMainNavView()
data object CreatePostNavView : SimpleNonMainNavView()
data object SettingsNavView : SimpleNonMainNavView()
data object SearchNavView : SimpleNonMainNavView()


sealed class AdvancedNonMainNavView : NonMainNavView()
data class ThreadNavView(val nip19Event: Nip19Event) : AdvancedNonMainNavView()
data class ProfileNavView(val nip19Profile: Nip19Profile) : AdvancedNonMainNavView()
data class TopicNavView(val topic: Topic) : AdvancedNonMainNavView()
