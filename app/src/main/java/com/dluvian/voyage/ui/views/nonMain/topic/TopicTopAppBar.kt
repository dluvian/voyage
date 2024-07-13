package com.dluvian.voyage.ui.views.nonMain.topic

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
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.MuteTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.core.UnmuteTopic
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.dropdown.SimpleDropdownItem
import com.dluvian.voyage.ui.theme.HorizMoreIcon

@Composable
fun TopicTopAppBar(topic: Topic, isFollowed: Boolean, isMuted: Boolean, onUpdate: OnUpdate) {
    SimpleGoBackTopAppBar(
        title = "#$topic",
        actions = {
            ActionButton(
                topic = topic,
                isMuted = isMuted,
                onUpdate = onUpdate
            )
            if (!isMuted) FollowButton(
                isFollowed = isFollowed,
                onFollow = { onUpdate(FollowTopic(topic = topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic = topic)) })
        },
        onUpdate = onUpdate
    )
}

@Composable
private fun ActionButton(
    topic: Topic,
    isMuted: Boolean,
    onUpdate: OnUpdate
) {
    val showMenu = remember { mutableStateOf(false) }

    Box {
        ActionMenu(
            isExpanded = showMenu.value,
            topic = topic,
            isMuted = isMuted,
            onUpdate = onUpdate,
            onDismiss = { showMenu.value = false })
        IconButton(onClick = { showMenu.value = true }) {
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
    topic: Topic,
    isMuted: Boolean,
    onUpdate: OnUpdate,
    onDismiss: Fn
) {
    DropdownMenu(expanded = isExpanded, onDismissRequest = onDismiss) {
        if (isMuted) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unmute),
                onClick = {
                    onUpdate(UnmuteTopic(topic = topic))
                    onDismiss()
                })
        } else {
            SimpleDropdownItem(
                text = stringResource(id = R.string.mute),
                onClick = {
                    onUpdate(MuteTopic(topic = topic))
                    onDismiss()
                })
        }
    }
}
