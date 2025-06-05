package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.runtime.Composable
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FollowTopic
import com.dluvian.voyage.model.UnfollowTopic
import com.dluvian.voyage.ui.components.bar.SimpleGoBackTopAppBar
import com.dluvian.voyage.ui.components.button.FollowButton

@Composable
fun TopicTopAppBar(
    topic: Topic,
    isFollowed: Boolean,
    onUpdate: (Cmd) -> Unit
) {
    SimpleGoBackTopAppBar(
        title = "#$topic",
        actions = {
            FollowButton(
                isFollowed = isFollowed,
                onFollow = { onUpdate(FollowTopic(topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic)) })
        },
        onUpdate = onUpdate
    )
}
