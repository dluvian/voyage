package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.runtime.Composable
import com.dluvian.voyage.Core
import com.dluvian.voyage.navigator.BookmarkNavView
import com.dluvian.voyage.navigator.CreateGitIssueNavView
import com.dluvian.voyage.navigator.CreatePostNavView
import com.dluvian.voyage.navigator.CrossPostNavView
import com.dluvian.voyage.navigator.EditProfileNavView
import com.dluvian.voyage.navigator.FollowListsNavView
import com.dluvian.voyage.navigator.NonMainNavView
import com.dluvian.voyage.navigator.ProfileNavView
import com.dluvian.voyage.navigator.RelayEditorNavView
import com.dluvian.voyage.navigator.RelayProfileNavView
import com.dluvian.voyage.navigator.ReplyNavView
import com.dluvian.voyage.navigator.SettingsNavView
import com.dluvian.voyage.navigator.ThreadNavView
import com.dluvian.voyage.navigator.ThreadNeventNavView
import com.dluvian.voyage.navigator.TopicNavView
import com.dluvian.voyage.ui.views.nonMain.profile.ProfileView
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
            searchSuggestions = core.appContainer.suggestionProvider.profileSuggestions,
            topicSuggestions = core.appContainer.suggestionProvider.topicSuggestions,
            onUpdate = core.onUpdate
        )

        SettingsNavView -> SettingsView(
            vm = core.vmContainer.settingsVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ProfileNavView -> ProfileView(
            vm = core.vmContainer.profileVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ThreadNavView, is ThreadNeventNavView -> ThreadView(
            vm = core.vmContainer.threadVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is TopicNavView -> TopicView(
            vm = core.vmContainer.topicVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ReplyNavView -> CreateReplyView(
            vm = core.vmContainer.createReplyVM,
            snackbar = core.appContainer.snackbar,
            searchSuggestions = core.appContainer.suggestionProvider.profileSuggestions,
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

        is CrossPostNavView -> CreateCrossPostView(
            vm = core.vmContainer.createCrossPostVM,
            topicSuggestions = core.appContainer.suggestionProvider.topicSuggestions,
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

        BookmarkNavView -> BookmarksView(
            vm = core.vmContainer.bookmarkVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        CreateGitIssueNavView -> CreateGitIssueView(
            vm = core.vmContainer.createGitIssueVM,
            snackbar = core.appContainer.snackbar,
            searchSuggestions = core.appContainer.suggestionProvider.profileSuggestions,
            onUpdate = core.onUpdate
        )
    }
}
