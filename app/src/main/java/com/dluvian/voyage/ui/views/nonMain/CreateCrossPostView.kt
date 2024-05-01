package com.dluvian.voyage.ui.views.nonMain


import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.viewModel.CreateCrossPostViewModel
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold

@Composable
fun CreateCrossPostView(
    vm: CreateCrossPostViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
//    val isSending by vm.isSending
//    val id by vm.id
    val context = LocalContext.current
    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)

    SimpleGoBackScaffold(
        header = stringResource(id = R.string.cross_post),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        CreateCrossPostViewContent(
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateCrossPostViewContent(
    onUpdate: OnUpdate,
) {
    ComingSoon()
}
