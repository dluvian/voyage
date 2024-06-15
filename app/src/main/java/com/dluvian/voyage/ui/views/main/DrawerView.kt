package com.dluvian.voyage.ui.views.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.dluvian.voyage.core.ClickCreateList
import com.dluvian.voyage.core.ClickFollowLists
import com.dluvian.voyage.core.ClickRelayEditor
import com.dluvian.voyage.core.CloseDrawer
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.DrawerViewSubscribeSets
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenList
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.viewModel.DrawerViewModel
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.BookmarksIcon
import com.dluvian.voyage.ui.theme.ListIcon
import com.dluvian.voyage.ui.theme.RelayIcon
import com.dluvian.voyage.ui.theme.ViewListIcon
import com.dluvian.voyage.ui.theme.spacing
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
    ModalNavigationDrawer(
        drawerState = vm.drawerState,
        drawerContent = {
            ModalDrawerSheet {
                LaunchedEffect(key1 = vm.drawerState.isOpen) {
                    if (vm.drawerState.isOpen) onUpdate(DrawerViewSubscribeSets)
                }
                LazyColumn {
                    item { Spacer(modifier = Modifier.height(spacing.screenEdge)) }
                    item {
                        DrawerItem(
                            label = personalProfile.name,
                            icon = AccountIcon,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            onClick = {
                                onUpdate(
                                    OpenProfile(nprofile = createNprofile(hex = personalProfile.pubkey))
                                )
                                onUpdate(CloseDrawer(scope = scope))
                            }
                        )
                    }
                    item {
                        DrawerItem(
                            label = stringResource(id = R.string.follow_lists),
                            icon = ListIcon,
                            onClick = {
                                onUpdate(ClickFollowLists)
                                onUpdate(CloseDrawer(scope = scope))
                            }
                        )
                    }
                    item {
                        DrawerItem(
                            label = stringResource(id = R.string.bookmarks),
                            icon = BookmarksIcon,
                            onClick = {
                                onUpdate(ClickBookmarks)
                                onUpdate(CloseDrawer(scope = scope))
                            }
                        )
                    }
                    item {
                        DrawerItem(
                            label = stringResource(id = R.string.relays),
                            icon = RelayIcon,
                            onClick = {
                                onUpdate(ClickRelayEditor)
                                onUpdate(CloseDrawer(scope = scope))
                            }
                        )
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = spacing.medium)
                        )
                    }
                    items(itemSets) {
                        DrawerItem(
                            label = it.title,
                            icon = ViewListIcon,
                            onClick = {
                                onUpdate(OpenList(identifier = it.identifier))
                                onUpdate(CloseDrawer(scope = scope))
                            }
                        )
                    }
                    item {
                        DrawerItem(
                            label = stringResource(id = R.string.create_a_list),
                            icon = AddIcon,
                            onClick = {
                                onUpdate(ClickCreateList)
                                onUpdate(CloseDrawer(scope = scope))
                            }
                        )
                    }
                }
            }
        }
    ) {
        content()
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
