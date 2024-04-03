package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun ProfileScaffold(
    profile: FullProfileUI,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            ProfileTopAppBar(
                profile = profile,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
