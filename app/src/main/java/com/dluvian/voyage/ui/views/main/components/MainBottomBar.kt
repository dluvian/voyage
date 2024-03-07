package com.dluvian.voyage.ui.views.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickCreate
import com.dluvian.voyage.core.ClickHome
import com.dluvian.voyage.core.ClickInbox
import com.dluvian.voyage.core.ClickSettings
import com.dluvian.voyage.core.ClickTopics
import com.dluvian.voyage.core.Lambda
import com.dluvian.voyage.core.UIEvent
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.MainNavView
import com.dluvian.voyage.core.navigation.TopicsNavView
import com.dluvian.voyage.ui.AddIcon
import com.dluvian.voyage.ui.HomeIcon
import com.dluvian.voyage.ui.InboxIcon
import com.dluvian.voyage.ui.SettingsIcon
import com.dluvian.voyage.ui.TopicIcon

@Composable
fun MainBottomBar(currentView: MainNavView, onUpdate: (UIEvent) -> Unit) {
    NavigationBar {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MainBottomBarItem(
                selected = currentView is HomeNavView,
                label = stringResource(id = R.string.home),
                icon = HomeIcon,
                onClick = { onUpdate(ClickHome) })
            MainBottomBarItem(
                selected = currentView is TopicsNavView,
                label = stringResource(id = R.string.topics),
                icon = TopicIcon,
                onClick = { onUpdate(ClickTopics) })
            MainBottomBarItem(
                selected = false,
                label = stringResource(id = R.string.create),
                icon = AddIcon,
                onClick = { onUpdate(ClickCreate) })
            MainBottomBarItem(
                selected = currentView is InboxNavView,
                label = stringResource(id = R.string.inbox),
                icon = InboxIcon,
                onClick = { onUpdate(ClickInbox) })
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
    onClick: Lambda
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        icon = { Icon(imageVector = icon, contentDescription = label) }
    )
}
