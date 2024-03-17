package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.core.navigator.TopicNavView
import com.dluvian.voyage.ui.components.scaffold.NonMainScaffold
import com.dluvian.voyage.ui.views.nonMain.subViews.CreatePostView
import com.dluvian.voyage.ui.views.nonMain.subViews.ProfileView
import com.dluvian.voyage.ui.views.nonMain.subViews.SearchView
import com.dluvian.voyage.ui.views.nonMain.subViews.SettingsView
import com.dluvian.voyage.ui.views.nonMain.subViews.ThreadView
import com.dluvian.voyage.ui.views.nonMain.subViews.TopicView

@Composable
fun NonMainView(
    core: Core,
    currentView: NonMainNavView,
) {
    NonMainScaffold(
        currentView = currentView,
        snackBarHostState = core.appContainer.snackbarHostState,
        isLoading = core.vmContainer.settingsVM.isLoadingAccount.value,
        onUpdate = core.onUpdate
    ) {
        when (currentView) {
            CreatePostNavView -> CreatePostView()
            SettingsNavView -> SettingsView(
                vm = core.vmContainer.settingsVM,
                onUpdate = core.onUpdate
            )

            SearchNavView -> SearchView(vm = core.vmContainer.searchVM, onUpdate = core.onUpdate)
            is ProfileNavView -> ProfileView(
                vm = core.vmContainer.profileVM,
                onUpdate = core.onUpdate
            )

            is ThreadNavView -> ThreadView(vm = core.vmContainer.threadVM, onUpdate = core.onUpdate)
            is TopicNavView -> TopicView(vm = core.vmContainer.topicVM, onUpdate = core.onUpdate)
        }
    }
}
