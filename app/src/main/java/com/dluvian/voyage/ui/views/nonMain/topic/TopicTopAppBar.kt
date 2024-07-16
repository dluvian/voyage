package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.button.ProfileOrTopicOptionButton

@Composable
fun TopicTopAppBar(
    topic: Topic,
    isFollowed: Boolean,
    isMuted: Boolean,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    onUpdate: OnUpdate
) {
    SimpleGoBackTopAppBar(
        title = "#$topic",
        actions = {
            ProfileOrTopicOptionButton(
                item = ItemSetTopic(topic = topic),
                isMuted = isMuted,
                addableLists = addableLists,
                nonAddableLists = nonAddableLists,
                scope = rememberCoroutineScope(),
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
