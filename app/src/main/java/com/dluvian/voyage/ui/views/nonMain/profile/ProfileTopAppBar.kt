package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.AddProfileToList
import com.dluvian.voyage.core.ClickEditProfile
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewLoadLists
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.ui.components.NamedCheckbox
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.dropdown.SimpleDropdownItem
import com.dluvian.voyage.ui.theme.HorizMoreIcon
import kotlinx.coroutines.CoroutineScope


@Composable
fun ProfileTopAppBar(
    profile: FullProfileUI,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val scope = rememberCoroutineScope()
    SimpleGoBackTopAppBar(
        title = profile.inner.name,
        actions = {
            if (!profile.inner.isMe) {
                ActionButton(
                    pubkey = profile.inner.pubkey,
                    addableLists = addableLists,
                    nonAddableLists = nonAddableLists,
                    snackbar = snackbar,
                    scope = scope,
                    onUpdate = onUpdate
                )
                FollowButton(
                    isFollowed = profile.inner.isFriend,
                    onFollow = {
                        onUpdate(FollowProfile(pubkey = profile.inner.pubkey))
                    },
                    onUnfollow = {
                        onUpdate(UnfollowProfile(pubkey = profile.inner.pubkey))
                    })
            } else {
                Button(onClick = { onUpdate(ClickEditProfile) }) {
                    Text(text = stringResource(id = R.string.edit))
                }
            }
        },
        onUpdate = onUpdate
    )
}

@Composable
private fun ActionButton(
    pubkey: PubkeyHex,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    onUpdate: OnUpdate
) {
    val showMenu = remember { mutableStateOf(false) }

    Box {
        ActionMenu(
            isExpanded = showMenu.value,
            pubkey = pubkey,
            addableLists = addableLists,
            nonAddableLists = nonAddableLists,
            scope = scope,
            onUpdate = onUpdate,
            onDismiss = { showMenu.value = false })
        IconButton(
            onClick = {
                showMenu.value = true
                onUpdate(ProfileViewLoadLists)
            }
        ) {
            Icon(
                imageVector = HorizMoreIcon,
                contentDescription = stringResource(id = R.string.options)
            )
        }
    }
}

@Composable
private fun ActionMenu(
    isExpanded: Boolean,
    pubkey: PubkeyHex,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    scope: CoroutineScope,
    onUpdate: OnUpdate,
    onDismiss: Fn
) {
    val showAddToList = remember { mutableStateOf(false) }

    if (showAddToList.value) AddToListDialog(
        pubkey = pubkey,
        addableLists = addableLists,
        nonAddableLists = nonAddableLists,
        scope = scope,
        onUpdate = onUpdate,
        onDismiss = { showAddToList.value = false }
    )

    DropdownMenu(expanded = isExpanded, onDismissRequest = onDismiss) {
        SimpleDropdownItem(
            text = stringResource(id = R.string.add_to_list),
            onClick = {
                showAddToList.value = true
                onDismiss()
            })
    }
}

@Composable
private fun AddToListDialog(
    pubkey: PubkeyHex,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    scope: CoroutineScope,
    onUpdate: OnUpdate,
    onDismiss: Fn
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = stringResource(id = R.string.choose_list)) },
        text = {
            LazyColumn {
                items(addableLists) { listMeta ->
                    NamedCheckbox(
                        isChecked = false,
                        name = listMeta.title,
                        onClick = {
                            onUpdate(
                                AddProfileToList(
                                    pubkey = pubkey,
                                    identifier = listMeta.identifier,
                                    scope = scope,
                                    context = context
                                )
                            )
                            onDismiss()
                        })
                }
                items(nonAddableLists) { listMeta ->
                    NamedCheckbox(
                        isChecked = true,
                        name = listMeta.title,
                        isEnabled = false,
                        onClick = { })
                }
            }
        }
    )
}
