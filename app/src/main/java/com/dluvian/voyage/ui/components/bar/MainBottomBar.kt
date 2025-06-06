package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.R
import com.dluvian.voyage.model.ClickCreate
import com.dluvian.voyage.model.ClickDiscover
import com.dluvian.voyage.model.ClickHome
import com.dluvian.voyage.model.ClickInbox
import com.dluvian.voyage.model.ClickSearch
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.navigator.DiscoverNavView
import com.dluvian.voyage.navigator.HomeNavView
import com.dluvian.voyage.navigator.InboxNavView
import com.dluvian.voyage.navigator.MainNavView
import com.dluvian.voyage.navigator.SearchNavView
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.DiscoverIcon
import com.dluvian.voyage.ui.theme.HomeIcon
import com.dluvian.voyage.ui.theme.InboxIcon
import com.dluvian.voyage.ui.theme.SearchIcon
import kotlinx.coroutines.launch

@Composable
fun MainBottomBar(
    currentView: MainNavView,
    homeFeedState: LazyListState,
    inboxFeedState: LazyListState,
    onUpdate: (Cmd) -> Unit
) {
    NavigationBar(modifier = Modifier
        .navigationBarsPadding()
        .height(52.dp)) {
        val scope = rememberCoroutineScope()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MainBottomBarItem(
                selected = currentView is HomeNavView,
                description = stringResource(id = R.string.home),
                icon = HomeIcon,
                onClick = {
                    onUpdate(ClickHome)
                    if (currentView is HomeNavView) {
                        scope.launch { homeFeedState.animateScrollToItem(index = 0) }
                    }
                })
            MainBottomBarItem(
                selected = currentView is DiscoverNavView,
                description = stringResource(id = R.string.discover),
                icon = DiscoverIcon,
                onClick = { onUpdate(ClickDiscover) })
            MainBottomBarItem(
                selected = false,
                description = stringResource(id = R.string.create),
                icon = AddIcon,
                onClick = { onUpdate(ClickCreate) })
            MainBottomBarItem(
                selected = currentView is SearchNavView,
                description = stringResource(id = R.string.search),
                icon = SearchIcon,
                onClick = { onUpdate(ClickSearch) })
            MainBottomBarItem(
                selected = currentView is InboxNavView,
                description = stringResource(id = R.string.inbox),
                icon = InboxIcon,
                onClick = {
                    onUpdate(ClickInbox)
                    if (currentView is InboxNavView) {
                        scope.launch { inboxFeedState.animateScrollToItem(index = 0) }
                    }
                })
        }
    }
}

@Composable
private fun RowScope.MainBottomBarItem(
    selected: Boolean,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = description) }
    )
}
