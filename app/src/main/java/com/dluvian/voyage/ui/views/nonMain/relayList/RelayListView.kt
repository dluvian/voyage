package com.dluvian.voyage.ui.views.nonMain.relayList

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.indicator.ComingSoon

@Composable
fun RelayListView(snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    RelayListScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        ComingSoon()
    }
}
