package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.ui.components.button.footer.FooterIconButton
import com.dluvian.voyage.ui.components.dropdown.FeedItemDropdown
import com.dluvian.voyage.ui.theme.HorizMoreIcon
import com.dluvian.voyage.ui.theme.OnBgLight

@Composable
fun OptionsButton(
    mainEvent: MainEvent,
    onUpdate: OnUpdate,
) {
    val showMenu = remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.CenterEnd) {
        FeedItemDropdown(
            isOpen = showMenu.value,
            mainEvent = mainEvent,
            onDismiss = { showMenu.value = false },
            onUpdate = onUpdate,
        )
        FooterIconButton(
            icon = HorizMoreIcon,
            description = stringResource(id = R.string.show_options_menu),
            color = OnBgLight,
            onClick = { showMenu.value = !showMenu.value })
    }
}
