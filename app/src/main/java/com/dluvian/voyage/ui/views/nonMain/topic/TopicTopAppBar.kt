package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.TopicViewFollowTopic
import com.dluvian.voyage.core.TopicViewUnfollowTopic
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.button.GoBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicTopAppBar(topic: Topic, isFollowed: Boolean, onUpdate: OnUpdate) {
    TopAppBar(
        title = {
            Text(text = "#$topic", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = {
            FollowButton(
                isFollowed = isFollowed,
                onFollow = { onUpdate(TopicViewFollowTopic(topic)) },
                onUnfollow = { onUpdate(TopicViewUnfollowTopic(topic)) })
        },
        navigationIcon = {
            GoBackIconButton(onUpdate = onUpdate)
        },
    )
}
