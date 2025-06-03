package com.dluvian.voyage.ui.views.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.ClickBookmarks
import com.dluvian.voyage.model.ClickFollowLists
import com.dluvian.voyage.model.ClickRelayEditor
import com.dluvian.voyage.model.ClickSettings
import com.dluvian.voyage.model.CloseDrawer
import com.dluvian.voyage.model.OpenProfile
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.BookmarksIcon
import com.dluvian.voyage.ui.theme.ListIcon
import com.dluvian.voyage.ui.theme.RelayIcon
import com.dluvian.voyage.ui.theme.SettingsIcon
import com.dluvian.voyage.ui.theme.light
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.DrawerViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun MainDrawer(
    vm: DrawerViewModel,
    scope: CoroutineScope,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    val personalProfile by vm.personalProfile.collectAsState()
    ModalNavigationDrawer(drawerState = vm.drawerState, drawerContent = {
        ModalDrawerSheet {
            LaunchedEffect(key1 = vm.drawerState.isOpen) {
                if (vm.drawerState.isOpen) onUpdate(DrawerViewSubscribeSets)
            }
            LazyColumn {
                item { Spacer(modifier = Modifier.height(spacing.screenEdge)) }
                item {
                    DrawerRow(
                        label = personalProfile.name,
                        icon = AccountIcon,
                        onClick = {
                            onUpdate(
                                OpenProfile(nprofile = createNprofile(hex = personalProfile.pubkey))
                            )
                            onUpdate(CloseDrawer(scope = scope))
                        })
                }
                item {
                    DrawerRow(
                        label = stringResource(id = R.string.follow_lists),
                        icon = ListIcon,
                        onClick = {
                            onUpdate(ClickFollowLists)
                            onUpdate(CloseDrawer(scope = scope))
                        })
                }
                item {
                    DrawerRow(
                        label = stringResource(id = R.string.bookmarks),
                        icon = BookmarksIcon,
                        onClick = {
                            onUpdate(ClickBookmarks)
                            onUpdate(CloseDrawer(scope = scope))
                        })
                }
                item {
                    DrawerRow(
                        label = stringResource(id = R.string.relays),
                        icon = RelayIcon,
                        onClick = {
                            onUpdate(ClickRelayEditor)
                            onUpdate(CloseDrawer(scope = scope))
                        })
                }
                item {
                    DrawerRow(
                        label = stringResource(id = R.string.settings),
                        icon = SettingsIcon,
                        onClick = {
                            onUpdate(ClickSettings)
                            onUpdate(CloseDrawer(scope = scope))
                        })
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.medium)
                    )
                }
            }
        }
    }) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: Fn,
    onLongClick: Fn = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.xl)
            .padding(start = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            tint = LocalContentColor.current.light(0.85f),
            contentDescription = label
        )
        Spacer(modifier = Modifier.width(spacing.xl))
        Text(text = label, color = LocalContentColor.current.light(0.85f))
    }
}
