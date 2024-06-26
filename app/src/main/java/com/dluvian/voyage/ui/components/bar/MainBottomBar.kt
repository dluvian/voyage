package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickCreate
import com.dluvian.voyage.core.ClickDiscover
import com.dluvian.voyage.core.ClickHome
import com.dluvian.voyage.core.ClickInbox
import com.dluvian.voyage.core.ClickSettings
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.UIEvent
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.DiscoverIcon
import com.dluvian.voyage.ui.theme.HomeIcon
import com.dluvian.voyage.ui.theme.InboxIcon
import com.dluvian.voyage.ui.theme.SettingsIcon
import kotlinx.coroutines.launch

@Composable
fun MainBottomBar(
    currentView: MainNavView,
    homeFeedState: LazyListState,
    inboxFeedState: LazyListState,
    onUpdate: (UIEvent) -> Unit
) {
    NavigationBar {
        val scope = rememberCoroutineScope()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MainBottomBarItem(
                selected = currentView is HomeNavView,
                label = stringResource(id = R.string.home),
                icon = HomeIcon,
                onClick = {
                    onUpdate(ClickHome)
                    if (currentView is HomeNavView) {
                        scope.launch { homeFeedState.animateScrollToItem(index = 0) }
                    }
                })
            MainBottomBarItem(
                selected = currentView is DiscoverNavView,
                label = stringResource(id = R.string.discover),
                icon = DiscoverIcon,
                onClick = { onUpdate(ClickDiscover) })
            MainBottomBarItem(
                selected = false,
                label = stringResource(id = R.string.create),
                icon = AddIcon,
                onClick = { onUpdate(ClickCreate) })
            MainBottomBarItem(
                selected = currentView is InboxNavView,
                label = stringResource(id = R.string.inbox),
                icon = InboxIcon,
                onClick = {
                    onUpdate(ClickInbox)
                    if (currentView is InboxNavView) {
                        scope.launch { inboxFeedState.animateScrollToItem(index = 0) }
                    }
                })
            MainBottomBarItem(
                selected = false,
                label = stringResource(id = R.string.settings),
                icon = SettingsIcon,
                onClick = { onUpdate(ClickSettings) })
        }
    }
}

@Composable
private fun RowScope.MainBottomBarItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: Fn
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        icon = { Icon(imageVector = icon, contentDescription = label) }
    )
}
