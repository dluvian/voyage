package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.core.navigator.SearchNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.ui.components.scaffold.NonMainScaffold
import com.dluvian.voyage.ui.views.nonMain.subViews.CreatePostView
import com.dluvian.voyage.ui.views.nonMain.subViews.SearchView
import com.dluvian.voyage.ui.views.nonMain.subViews.SettingsView

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
        }
    }
}
