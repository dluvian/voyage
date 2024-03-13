package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.navigator.CreatePostNavView
import com.dluvian.voyage.core.navigator.NonMainNavView
import com.dluvian.voyage.core.navigator.SettingsNavView
import com.dluvian.voyage.ui.components.scaffold.NonMainScaffold
import com.dluvian.voyage.ui.views.nonMain.subViews.CreatePostView
import com.dluvian.voyage.ui.views.nonMain.subViews.SettingsView

@Composable
fun NonMainView(
    core: Core,
    currentView: NonMainNavView,
) {
    NonMainScaffold(
        currentView = currentView,
        snackBarHostState = core.snackbarHostState,
        isLoading = core.settingsViewModel.isLoadingAccount.value,
        onUpdate = core.onUpdate
    ) {
        when (currentView) {
            CreatePostNavView -> CreatePostView()
            SettingsNavView -> SettingsView(vm = core.settingsViewModel, onUpdate = core.onUpdate)
        }
    }
}
