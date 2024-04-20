package com.dluvian.voyage.ui.views.nonMain.relayList

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold

@Composable
fun RelayListView(snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.relay_list),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ComingSoon()
    }
}
