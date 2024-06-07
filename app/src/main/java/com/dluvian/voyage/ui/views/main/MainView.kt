package com.dluvian.voyage.ui.views.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickBookmarks
import com.dluvian.voyage.core.ClickFollowLists
import com.dluvian.voyage.core.ClickRelayEditor
import com.dluvian.voyage.core.CloseDrawer
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.BookmarksIcon
import com.dluvian.voyage.ui.theme.ListIcon
import com.dluvian.voyage.ui.theme.RelayIcon
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.main.subViews.DiscoverView
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView
import kotlinx.coroutines.CoroutineScope

@Composable
fun MainView(
    core: Core,
    scope: CoroutineScope,
    currentView: MainNavView,
) {
    val personalProfile by core.vmContainer.drawerVM.personalProfile.collectAsState()
    ModalNavigationDrawer(
        drawerState = core.vmContainer.drawerVM.drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(spacing.screenEdge))
                DrawerItem(
                    label = personalProfile.name,
                    icon = AccountIcon,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    onClick = {
                        core.onUpdate(
                            OpenProfile(nprofile = createNprofile(hex = personalProfile.pubkey))
                        )
                        core.onUpdate(CloseDrawer(scope = scope))
                    }
                )
                DrawerItem(
                    label = stringResource(id = R.string.follow_lists),
                    icon = ListIcon,
                    onClick = {
                        core.onUpdate(ClickFollowLists)
                        core.onUpdate(CloseDrawer(scope = scope))
                    }
                )
                DrawerItem(
                    label = stringResource(id = R.string.bookmarks),
                    icon = BookmarksIcon,
                    onClick = {
                        core.onUpdate(ClickBookmarks)
                        core.onUpdate(CloseDrawer(scope = scope))
                    }
                )
                DrawerItem(
                    label = stringResource(id = R.string.relays),
                    icon = RelayIcon,
                    onClick = {
                        core.onUpdate(ClickRelayEditor)
                        core.onUpdate(CloseDrawer(scope = scope))
                    }
                )
            }
        }
    ) {
        ScreenContent(currentView = currentView, core = core)
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    style: TextStyle = LocalTextStyle.current,
    onClick: Fn
) {
    NavigationDrawerItem(
        icon = {
            Icon(imageVector = icon, contentDescription = null)
        },
        label = {
            Text(text = label, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        selected = false,
        onClick = onClick
    )
}

@Composable
private fun ScreenContent(currentView: MainNavView, core: Core) {
    MainScaffold(
        currentView = currentView,
        snackbar = core.appContainer.snackbar,
        homeFeedState = core.vmContainer.homeVM.feedState,
        inboxFeedState = core.vmContainer.inboxVM.feedState,
        onUpdate = core.onUpdate
    ) {
        when (currentView) {
            HomeNavView -> HomeView(vm = core.vmContainer.homeVM, onUpdate = core.onUpdate)
            InboxNavView -> InboxView(vm = core.vmContainer.inboxVM, onUpdate = core.onUpdate)
            DiscoverNavView -> DiscoverView(
                vm = core.vmContainer.discoverVM,
                onUpdate = core.onUpdate
            )
        }
    }
}
