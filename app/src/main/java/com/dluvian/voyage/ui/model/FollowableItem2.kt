package com.dluvian.voyage.ui.model

import androidx.compose.material3.Icon
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.theme.HashtagIcon

// TODO: Resolve name collision with FollowableItem
sealed class FollowableItem2(
    open val label: String,
    open val icon: ComposableContent,
    open val button: ComposableContent,
    open val onOpen: Fn,
)

sealed class FollowableItem(
    override val label: String,
    override val icon: ComposableContent,
    override val button: ComposableContent,
    override val onOpen: Fn,
) : FollowableItem2(
    label = label,
    icon = icon,
    button = button,
    onOpen = onOpen
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
            isEnabled = profile.isFriend || !profile.isLocked,
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
