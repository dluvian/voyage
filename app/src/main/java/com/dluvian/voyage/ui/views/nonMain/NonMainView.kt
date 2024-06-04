package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.BookmarksNavView
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.CrossPostCreationNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.FollowListsNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.RelayEditorNavView
import com.dluvian.voyage.core.navigator.RelayProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.ThreadRawNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.views.nonMain.profile.ProfileView
import com.dluvian.voyage.ui.views.nonMain.search.SearchView
import com.dluvian.voyage.ui.views.nonMain.topic.TopicView

@Composable
fun NonMainView(
    core: Core,
    currentView: NonMainNavView,
) {
    when (currentView) {
        CreatePostNavView -> CreatePostView(
            vm = core.vmContainer.createPostVM,
            snackbar = core.appContainer.snackbar,
            searchSuggestions = core.appContainer.profileSuggestionProvider.suggestions,
            onUpdate = core.onUpdate
        )

        SettingsNavView -> SettingsView(
            vm = core.vmContainer.settingsVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        SearchNavView -> SearchView(
            vm = core.vmContainer.searchVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ProfileNavView -> ProfileView(
            vm = core.vmContainer.profileVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ThreadNavView, is ThreadRawNavView -> ThreadView(
            vm = core.vmContainer.threadVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is TopicNavView -> TopicView(
            vm = core.vmContainer.topicVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ReplyCreationNavView -> CreateReplyView(
            vm = core.vmContainer.createReplyVM,
            snackbar = core.appContainer.snackbar,
            searchSuggestions = core.appContainer.profileSuggestionProvider.suggestions,
            onUpdate = core.onUpdate
        )

        EditProfileNavView -> EditProfileView(
            vm = core.vmContainer.editProfileVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        RelayEditorNavView -> RelayEditorView(
            vm = core.vmContainer.relayEditorVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is CrossPostCreationNavView -> CreateCrossPostView(
            vm = core.vmContainer.createCrossPostVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is RelayProfileNavView -> RelayProfileView(
            vm = core.vmContainer.relayProfileVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        FollowListsNavView -> FollowListsView(
            vm = core.vmContainer.followListsVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        BookmarksNavView -> ComingSoon()
    }
}
