package com.dluvian.voyage.ui.views.nonMain.relayList

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold


@Composable
fun RelayListScaffold(snackbar: SnackbarHostState, onUpdate: OnUpdate, content: ComposableContent) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            SimpleGoBackTopAppBar(
                title = stringResource(id = R.string.relay_list),
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
