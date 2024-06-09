package com.dluvian.voyage.ui.components.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.button.GoBackIconButton
import com.dluvian.voyage.ui.components.button.MenuIconButton
import com.dluvian.voyage.ui.components.button.SearchIconButton
import com.dluvian.voyage.ui.components.indicator.TopBarCircleProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageTopAppBar(
    title: String? = null,
    showGoBack: Boolean = false,
    showMenu: Boolean = false,
    isLoading: Boolean = false,
    hasSearch: Boolean = false,
    onUpdate: OnUpdate = {}
) {
    TopAppBar(
        title = {
            title?.let {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showMenu) MenuIconButton(onUpdate = onUpdate)
            else if (showGoBack) GoBackIconButton(onUpdate = onUpdate)
        },
        actions = {
            if (hasSearch) SearchIconButton(onUpdate = onUpdate)
            if (isLoading) TopBarCircleProgressIndicator()
        }
    )
}
