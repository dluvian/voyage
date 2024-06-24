package com.dluvian.voyage.ui.views.nonMain.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.ListViewModel
import com.dluvian.voyage.ui.components.indicator.ComingSoon

@Composable
fun ListView(
    vm: ListViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val title by vm.itemSetProvider.title
    val identifier by vm.itemSetProvider.identifier

    ListScaffold(title = title, identifier = identifier, snackbar = snackbar, onUpdate = onUpdate) {
        ComingSoon()
    }
}
