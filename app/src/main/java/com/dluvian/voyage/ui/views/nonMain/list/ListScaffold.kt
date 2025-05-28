package com.dluvian.voyage.ui.views.nonMain.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.cmd.EditList
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.EditIconButton
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun ListScaffold(
    title: String,
    identifier: String,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            SimpleGoBackTopAppBar(
                title = title,
                actions = {
                    EditIconButton(onEdit = {
                        onUpdate(EditList(identifier = identifier))
                    })
                },
                onUpdate = onUpdate,
            )
        }
    ) {
        content()
    }
}
