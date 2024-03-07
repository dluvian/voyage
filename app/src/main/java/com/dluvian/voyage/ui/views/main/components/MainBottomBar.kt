package com.dluvian.voyage.ui.views.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickCreate
import com.dluvian.voyage.core.ClickHome
import com.dluvian.voyage.core.ClickInbox
import com.dluvian.voyage.core.ClickSettings
import com.dluvian.voyage.core.ClickTopics
import com.dluvian.voyage.core.UIEvent
import com.dluvian.voyage.core.navigation.HomeNavView
import com.dluvian.voyage.core.navigation.InboxNavView
import com.dluvian.voyage.core.navigation.MainNavView
import com.dluvian.voyage.core.navigation.SettingsNavView
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
            NavigationBarItem(
                selected = currentView is HomeNavView,
                onClick = { onUpdate(ClickHome) },
                label = { Text(text = stringResource(id = R.string.home)) },
                icon = { Icon(imageVector = HomeIcon, contentDescription = null) }
            )
            NavigationBarItem(
                selected = currentView is TopicsNavView,
                onClick = { onUpdate(ClickTopics) },
                label = { Text(text = stringResource(id = R.string.topics)) },
                icon = { Icon(imageVector = TopicIcon, contentDescription = null) }
            )
            NavigationBarItem(
                selected = false,
                onClick = { onUpdate(ClickCreate) },
                label = { Text(text = stringResource(id = R.string.create)) },
                icon = { Icon(imageVector = AddIcon, contentDescription = null) }
            )
            NavigationBarItem(
                selected = currentView is InboxNavView,
                onClick = { onUpdate(ClickInbox) },
                label = { Text(text = stringResource(id = R.string.inbox)) },
                icon = { Icon(imageVector = InboxIcon, contentDescription = null) }
            )
            NavigationBarItem(
                selected = currentView is SettingsNavView,
                onClick = { onUpdate(ClickSettings) },
                label = { Text(text = stringResource(id = R.string.settings)) },
                icon = { Icon(imageVector = SettingsIcon, contentDescription = null) }
            )
        }
    }
}
