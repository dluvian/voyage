package com.dluvian.voyage.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R

sealed class NavView


sealed class MainNavView : NavView() {
    @Composable
    @Stable
    fun getTitle(): String {
        return when (this) {
            is HomeNavView -> stringResource(id = R.string.home)
            is InboxNavView -> stringResource(id = R.string.inbox)
            is SettingsNavView -> stringResource(id = R.string.settings)
            is TopicsNavView -> stringResource(id = R.string.topics)
        }
    }
}

data object HomeNavView : MainNavView()
data object TopicsNavView : MainNavView()
data object InboxNavView : MainNavView()
data object SettingsNavView : MainNavView()


sealed class NonMainNavView : NavView() {
    @Composable
    @Stable
    fun getTitle(): String? {
        return when (this) {
            is CreatePostNavView -> null
        }
    }
}

sealed class ClosableNonMainNavView : NonMainNavView()
data object CreatePostNavView : ClosableNonMainNavView()
