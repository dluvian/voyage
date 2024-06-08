package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold

@Composable
fun EditListView(snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    SimpleGoBackScaffold(
        header = "lol",
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ComingSoon()
    }
}
