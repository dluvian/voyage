package com.dluvian.voyage.ui.views.nonMain.editProfile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.EditProfileViewModel
import com.dluvian.voyage.ui.components.indicator.ComingSoon

@Composable
fun EditProfileView(
    vm: EditProfileViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    ComingSoon()
}