package com.dluvian.voyage.ui.components.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.core.HomeViewOpenFilter
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.button.MenuIconButton
import com.dluvian.voyage.ui.components.button.SearchIconButton
import com.dluvian.voyage.ui.theme.FilterIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    currentView: MainNavView,
    onUpdate: OnUpdate = {}
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
            SearchIconButton(onUpdate = onUpdate)
        }
    )
}

@Composable
private fun MainFilter(currentView: MainNavView, onUpdate: OnUpdate) {
    when (currentView) {
        HomeNavView -> {
            IconButton(onClick = { onUpdate(HomeViewOpenFilter) }) {
                Icon(
                    imageVector = FilterIcon,
                    contentDescription = stringResource(id = R.string.filter)
                )
            }
        }

        DiscoverNavView -> {}
        InboxNavView -> {}
    }
}
