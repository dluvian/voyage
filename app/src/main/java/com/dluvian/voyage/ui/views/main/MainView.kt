package com.dluvian.voyage.ui.views.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.main.subViews.DiscoverView
import com.dluvian.voyage.ui.views.main.subViews.HomeView
import com.dluvian.voyage.ui.views.main.subViews.InboxView
import kotlinx.coroutines.launch

@Composable
fun MainView(
    core: Core,
    currentView: MainNavView,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: Fn = { scope.launch { drawerState.close() } }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(spacing.screenEdge))
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "dluvian",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = false,
                    onClick = closeDrawer
                )
                NavigationDrawerItem(
                    label = { Text(text = "Topics") },
                    selected = false,
                    onClick = closeDrawer
                )
                NavigationDrawerItem(
                    label = { Text(text = "Contacts") },
                    selected = false,
                    onClick = closeDrawer
                )
                NavigationDrawerItem(
                    label = { Text(text = "Bookmarks") },
                    selected = false,
                    onClick = closeDrawer
                )
                NavigationDrawerItem(
                    label = { Text(text = "Likes") },
                    selected = false,
                    onClick = closeDrawer
                )
                FullHorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Custom list 1") },
                    selected = false,
                    onClick = closeDrawer
                )
            }
        }
    ) {
        ScreenContent(currentView = currentView, core = core)
    }
}

@Composable
private fun ScreenContent(currentView: MainNavView, core: Core) {
    MainScaffold(
        currentView = currentView,
        snackbar = core.appContainer.snackbar,
        homeFeedState = core.vmContainer.homeVM.feedState,
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
