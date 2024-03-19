package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewAppend
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.ui.components.Feed

@Composable
fun ProfileView(vm: ProfileViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    ProfileScaffold(vm = vm, snackbar = snackbar, onUpdate = onUpdate) {
        Feed(
            paginator = vm.paginator,
            onRefresh = { onUpdate(ProfileViewRefresh) },
            onAppend = { onUpdate(ProfileViewAppend) },
            onUpdate = onUpdate
        )
    }
}
