package com.dluvian.voyage.ui.model

import androidx.compose.material3.Icon
import com.dluvian.voyage.cmd.OpenProfile
import com.dluvian.voyage.cmd.OpenTopic
import com.dluvian.voyage.cmd.UnfollowProfile
import com.dluvian.voyage.cmd.UnfollowTopic
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowTopic
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.theme.HashtagIcon

Composable () ->Unit
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.theme.HashtagIcon

sealed class FollowableItem(
    open val label: String,
    open val icon: @Composable () -> Unit,
    open val button: @Composable () -> Unit,
    open val onOpen: Fn,
)

data class FollowableProfileItem(
    val profile: AdvancedProfileView,
    val onUpdate: OnUpdate,
) : FollowableItem(
    label = profile.name,
    icon = { TrustIcon(profile = profile) },
    button = {
        FollowButton(
            isFollowed = profile.isFriend,
            onFollow = { onUpdate(FollowProfile(pubkey = profile.pubkey)) },
            onUnfollow = { onUpdate(UnfollowProfile(pubkey = profile.pubkey)) },
        )
    },
    onOpen = { onUpdate(OpenProfile(nprofile = profile.toNip19())) }
)

data class FollowableTopicItem(
    val topic: Topic,
    val isFollowed: Boolean,
    val onUpdate: OnUpdate,
) : FollowableItem(
    label = topic,
    icon = { Icon(imageVector = HashtagIcon, contentDescription = null) },
    button = {
        FollowButton(
            isFollowed = isFollowed,
            onFollow = { onUpdate(FollowTopic(topic = topic)) },
            onUnfollow = { onUpdate(UnfollowTopic(topic = topic)) },
        )
    },
    onOpen = { onUpdate(OpenTopic(topic = topic)) }
)
