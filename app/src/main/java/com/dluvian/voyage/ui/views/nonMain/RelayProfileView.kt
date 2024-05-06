package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.RelayProfileViewModel
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold

@Composable
fun RelayProfileView(vm: RelayProfileViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val header by vm.relay

    SimpleGoBackScaffold(
        header = header,
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        RelayProfileView()
    }
}

@Composable
fun RelayProfileView(modifier: Modifier = Modifier) {
    ComingSoon()
}
