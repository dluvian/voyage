package com.dluvian.voyage.ui.views.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.ClickBookmarks
import com.dluvian.voyage.ClickCreateList
import com.dluvian.voyage.ClickFollowLists
import com.dluvian.voyage.ClickRelayEditor
import com.dluvian.voyage.ClickSettings
import com.dluvian.voyage.CloseDrawer
import com.dluvian.voyage.DeleteList
import com.dluvian.voyage.DrawerViewSubscribeSets
import com.dluvian.voyage.EditList
import com.dluvian.voyage.OpenList
import com.dluvian.voyage.OpenProfile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.filterSetting.ItemSetMeta
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.BookmarksIcon
import com.dluvian.voyage.ui.theme.ListIcon
import com.dluvian.voyage.ui.theme.RelayIcon
import com.dluvian.voyage.ui.theme.SettingsIcon
import com.dluvian.voyage.ui.theme.ViewListIcon
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
    val itemSets by vm.itemSetMetas.collectAsState()
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
                items(itemSets) {
                    DrawerListItem(meta = it, scope = scope, onUpdate = onUpdate)
                }
                item {
                    DrawerRow(
                        modifier = Modifier.padding(bottom = spacing.bottomPadding),
                        label = stringResource(id = R.string.create_a_list),
                        icon = AddIcon,
                        onClick = {
                            onUpdate(ClickCreateList)
                            onUpdate(CloseDrawer(scope = scope))
                        })
                }
            }
        }
    }) {
        content()
    }
}

@Composable
private fun DrawerListItem(meta: ItemSetMeta, scope: CoroutineScope, onUpdate: OnUpdate) {
    val showMenu = remember { mutableStateOf(false) }
    Box {
        ItemSetOptionsMenu(
            isExpanded = showMenu.value,
            identifier = meta.identifier,
            scope = scope,
            onDismiss = { showMenu.value = false },
            onUpdate = onUpdate
        )
        DrawerRow(
            label = meta.title,
            modifier = Modifier.fillMaxWidth(),
            icon = ViewListIcon,
            onClick = {
                onUpdate(OpenList(identifier = meta.identifier))
                onUpdate(CloseDrawer(scope = scope))
            },
            onLongClick = { showMenu.value = true }
        )
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

@Composable
private fun ItemSetOptionsMenu(
    isExpanded: Boolean,
    identifier: String,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    onDismiss: Fn,
    onUpdate: OnUpdate,
) {
    val onCloseDrawer = { onUpdate(CloseDrawer(scope = scope)) }
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.edit_list)) },
            onClick = {
                onUpdate(EditList(identifier = identifier))
                onCloseDrawer()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.delete_list)) },
            onClick = {
                onUpdate(DeleteList(ident = identifier, onCloseDrawer = onCloseDrawer))
                onDismiss()
            }
        )
    }
}
