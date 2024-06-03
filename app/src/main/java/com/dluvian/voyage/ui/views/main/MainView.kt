package com.dluvian.voyage.ui.views.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickFollowLists
import com.dluvian.voyage.core.ClickRelayEditor
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.navigator.DiscoverNavView
import com.dluvian.voyage.core.navigator.HomeNavView
import com.dluvian.voyage.core.navigator.InboxNavView
import com.dluvian.voyage.core.navigator.MainNavView
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.scaffold.MainScaffold
import com.dluvian.voyage.ui.theme.AccountIcon
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
    val personalProfile by core.vmContainer.drawerVM.personalProfile.collectAsState()
    val closeDrawer: Fn = { scope.launch { drawerState.close() } }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(spacing.screenEdge))
                NavigationDrawerItem(
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AccountIcon,
                                contentDescription = stringResource(id = R.string.open_my_profile)
                            )
                            Spacer(modifier = Modifier.width(spacing.medium))
                            Text(
                                text = personalProfile.name,
                                style = TextStyle(
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        core.onUpdate(
                            OpenProfile(nprofile = createNprofile(hex = personalProfile.pubkey))
                        )
                        closeDrawer()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.follow_lists)) },
                    selected = false,
                    onClick = {
                        core.onUpdate(ClickFollowLists)
                        closeDrawer()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Bookmarks") },
                    selected = false,
                    onClick = closeDrawer
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = R.string.relays)) },
                    selected = false,
                    onClick = {
                        core.onUpdate(ClickRelayEditor)
                        closeDrawer()
                    }
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
