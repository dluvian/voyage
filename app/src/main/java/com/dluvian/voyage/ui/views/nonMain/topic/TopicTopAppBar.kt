package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton

@Composable
fun TopicTopAppBar(topic: Topic, isFollowed: Boolean, onUpdate: OnUpdate) {
    SimpleGoBackTopAppBar(
        title = "#$topic",
        actions = {
            FollowButton(
                isFollowed = isFollowed,
                onFollow = { onUpdate(FollowTopic(topic = topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic = topic)) })
        },
        onUpdate = onUpdate
    )
}
