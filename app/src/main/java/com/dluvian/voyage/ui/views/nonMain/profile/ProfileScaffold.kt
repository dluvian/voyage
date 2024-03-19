package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun ProfileScaffold(
    vm: ProfileViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    val name by vm.name
    val pubkey by vm.pubkey
    val isFollowed by vm.isFollowed

    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            ProfileTopAppBar(
                name = name,
                pubkey = pubkey,
                isFollowed = isFollowed,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
