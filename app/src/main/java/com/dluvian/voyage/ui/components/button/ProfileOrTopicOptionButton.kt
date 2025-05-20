package com.dluvian.voyage.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.MuteProfile
import com.dluvian.voyage.core.MuteTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewLoadLists
import com.dluvian.voyage.core.TopicViewLoadLists
import com.dluvian.voyage.core.UnmuteProfile
import com.dluvian.voyage.core.UnmuteTopic
import com.dluvian.voyage.core.model.ItemSetItem
import com.dluvian.voyage.core.model.ItemSetProfile
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.ui.components.dialog.AddToListDialog
import com.dluvian.voyage.ui.components.dropdown.SimpleDropdownItem
import com.dluvian.voyage.ui.theme.HorizMoreIcon
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProfileOrTopicOptionButton(
    item: ItemSetItem,
    isMuted: Boolean,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    scope: CoroutineScope,
    onUpdate: OnUpdate
) {
    val showMenu = remember { mutableStateOf(false) }

    Box {
        ActionMenu(
            isExpanded = showMenu.value,
            item = item,
            isMuted = isMuted,
            addableLists = addableLists,
            nonAddableLists = nonAddableLists,
            scope = scope,
            onUpdate = onUpdate,
            onDismiss = { showMenu.value = false })
        IconButton(
            onClick = {
                showMenu.value = true
                when (item) {
                    is ItemSetProfile -> onUpdate(ProfileViewLoadLists)
                    is ItemSetTopic -> onUpdate(TopicViewLoadLists)
                }
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
    item: ItemSetItem,
    isMuted: Boolean,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    scope: CoroutineScope,
    onUpdate: OnUpdate,
    onDismiss: Fn
) {
    val showAddToList = remember { mutableStateOf(false) }
    if (showAddToList.value) AddToListDialog(
        item = item,
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
        if (isMuted) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unmute),
                onClick = {
                    when (item) {
                        is ItemSetProfile -> onUpdate(
                            UnmuteProfile(
                                pubkey = item.pubkey,
                                debounce = false
                            )
                        )

                        is ItemSetTopic -> onUpdate(
                            UnmuteTopic(
                                topic = item.topic,
                                debounce = false
                            )
                        )
                    }
                    onDismiss()
                })
        } else {
            SimpleDropdownItem(
                text = stringResource(id = R.string.mute),
                onClick = {
                    when (item) {
                        is ItemSetProfile -> onUpdate(
                            MuteProfile(
                                pubkey = item.pubkey,
                                debounce = false
                            )
                        )

                        is ItemSetTopic -> onUpdate(MuteTopic(topic = item.topic, debounce = false))
                    }
                    onDismiss()
                })
        }
    }
}
