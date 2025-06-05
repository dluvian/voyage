package com.dluvian.voyage.ui.components.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.navigator.DiscoverNavView
import com.dluvian.voyage.navigator.HomeNavView
import com.dluvian.voyage.navigator.InboxNavView
import com.dluvian.voyage.navigator.MainNavView
import com.dluvian.voyage.navigator.SearchNavView
import com.dluvian.voyage.ui.components.button.FilterIconButton
import com.dluvian.voyage.ui.components.button.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    currentView: MainNavView,
    onUpdate: (Cmd) -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = currentView.getTitle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            MenuIconButton(onUpdate = onUpdate)
        },
        actions = {
            MainFilter(currentView = currentView, onUpdate = onUpdate)
        }
    )
}

@Composable
private fun MainFilter(currentView: MainNavView, onUpdate: (Cmd) -> Unit) {
    when (currentView) {
        HomeNavView -> FilterIconButton(onClick = { onUpdate(HomeViewOpenFilter) })

        InboxNavView -> FilterIconButton(onClick = { onUpdate(InboxViewOpenFilter) })

        SearchNavView, DiscoverNavView -> {}
    }
}
