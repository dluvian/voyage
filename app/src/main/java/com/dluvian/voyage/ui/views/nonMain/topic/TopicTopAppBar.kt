package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.button.ProfileOrTopicOptionButton

@Composable
fun TopicTopAppBar(
    topic: Topic,
    isFollowed: Boolean,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    onUpdate: (Cmd) -> Unit
) {
    SimpleGoBackTopAppBar(
        title = "#$topic",
        actions = {
            ProfileOrTopicOptionButton(
                item = ItemSetTopic(topic = topic),
                addableLists = addableLists,
                nonAddableLists = nonAddableLists,
                scope = rememberCoroutineScope(),
                onUpdate = onUpdate
            )
            FollowButton(
                isFollowed = isFollowed,
                onFollow = { onUpdate(FollowTopic(topic = topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic = topic)) })
        },
        onUpdate = onUpdate
    )
}
