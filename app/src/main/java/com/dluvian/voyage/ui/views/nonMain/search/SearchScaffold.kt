package com.dluvian.voyage.ui.views.nonMain.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun SearchScaffold(
    snackbar: SnackbarHostState,
    bottomBar: @Composable () -> Unit = {},
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    VoyageScaffold(
        snackbar = snackbar,
        topBar = { SearchTopAppBar(focusRequester = focusRequester, onUpdate = onUpdate) },
        bottomBar = bottomBar,
    ) {
        content()
    }
}
