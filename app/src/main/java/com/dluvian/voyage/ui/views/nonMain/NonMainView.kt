package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigation.CreatePostNavView
import com.dluvian.voyage.core.navigation.NonMainNavView
import com.dluvian.voyage.core.navigation.SettingsNavView
import com.dluvian.voyage.ui.components.scaffold.NonMainScaffold
import com.dluvian.voyage.ui.views.nonMain.subViews.CreatePostView
import com.dluvian.voyage.ui.views.nonMain.subViews.SettingsView

@Composable
fun NonMainView(
    currentView: NonMainNavView,
    snackbarHostState: SnackbarHostState,
    onUpdate: OnUpdate
) {
    NonMainScaffold(
        currentView = currentView,
        snackBarHostState = snackbarHostState,
        onUpdate = onUpdate
    ) {
        when (currentView) {
            is CreatePostNavView -> CreatePostView()
            SettingsNavView -> SettingsView()
        }
    }
}
