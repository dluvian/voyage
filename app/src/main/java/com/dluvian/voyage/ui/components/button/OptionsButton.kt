package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.ui.components.dropdown.ParentRowDropdown
import com.dluvian.voyage.ui.theme.HorizMoreIcon
import com.dluvian.voyage.ui.theme.sizing

@Composable
fun OptionsButton(
    parent: IParentUI,
    onUpdate: OnUpdate,
) {
    val showMenu = remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.CenterEnd) {
        ParentRowDropdown(
            isOpen = showMenu.value,
            parent = parent,
            onDismiss = { showMenu.value = false },
            onUpdate = onUpdate,
        )
        IconButton(
            modifier = Modifier.size(sizing.iconButton),
            onClick = { showMenu.value = !showMenu.value }
        ) {
            Icon(
                imageVector = HorizMoreIcon,
                contentDescription = stringResource(id = R.string.show_options_menu)
            )
        }
    }
}
