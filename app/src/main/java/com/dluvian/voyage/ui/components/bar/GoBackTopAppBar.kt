package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.button.GoBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoBackTopAppBar(
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onUpdate: (Cmd) -> Unit
) {
    TopAppBar(
        title = title,
        actions = actions,
        navigationIcon = {
            GoBackIconButton(onUpdate = onUpdate)
        },
    )
}
