package com.dluvian.voyage.ui.components.button

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.OpenDrawer
import com.dluvian.voyage.ui.theme.MenuIcon

@Composable
fun MenuIconButton(onUpdate: (Cmd) -> Unit) {
    val scope = rememberCoroutineScope()
    IconButton(onClick = { onUpdate(OpenDrawer(scope = scope)) }) {
        Icon(
            imageVector = MenuIcon,
            contentDescription = stringResource(id = R.string.open_menu)
        )
    }
}
