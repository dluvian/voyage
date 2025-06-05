package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun ProfileScaffold(
    profile: TrustProfile,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit
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
