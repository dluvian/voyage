package com.dluvian.voyage.core.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.SimpleNip19Event
import com.dluvian.voyage.core.model.SimpleNip19Profile

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


sealed class AdvancedNonMainNavView : NonMainNavView()
data class ThreadNavView(val nip19Event: SimpleNip19Event) : AdvancedNonMainNavView()
data class ProfileNavView(val nip19Profile: SimpleNip19Profile) : AdvancedNonMainNavView()
data class TopicNavView(val topic: Topic) : AdvancedNonMainNavView()
