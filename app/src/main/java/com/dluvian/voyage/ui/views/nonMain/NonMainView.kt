package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.EditProfileNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.ReplyCreationNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.ui.views.nonMain.createPost.CreatePostView
import com.dluvian.voyage.ui.views.nonMain.createResponse.CreateResponseView
import com.dluvian.voyage.ui.views.nonMain.editProfile.EditProfileView
import com.dluvian.voyage.ui.views.nonMain.profile.ProfileView
import com.dluvian.voyage.ui.views.nonMain.search.SearchView
import com.dluvian.voyage.ui.views.nonMain.settings.SettingsView
import com.dluvian.voyage.ui.views.nonMain.thread.ThreadView
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

        is ThreadNavView -> ThreadView(
            vm = core.vmContainer.threadVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is TopicNavView -> TopicView(
            vm = core.vmContainer.topicVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        is ReplyCreationNavView -> CreateResponseView(
            vm = core.vmContainer.createReplyVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )

        EditProfileNavView -> EditProfileView(
            vm = core.vmContainer.editProfileVM,
            snackbar = core.appContainer.snackbar,
            onUpdate = core.onUpdate
        )
    }
}
