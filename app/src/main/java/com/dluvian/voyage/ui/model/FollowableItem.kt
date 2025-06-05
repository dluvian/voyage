package com.dluvian.voyage.ui.model

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowTopic
import com.dluvian.voyage.model.OpenNProfile
import com.dluvian.voyage.model.OpenProfile
import com.dluvian.voyage.model.OpenTopic
import com.dluvian.voyage.model.UnfollowTopic
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.theme.HashtagIcon
import rust.nostr.sdk.Nip19Profile

sealed class FollowableItem(
    open val label: String,
    open val icon: @Composable () -> Unit,
    open val button: @Composable () -> Unit,
    open val onOpen: () -> Unit,
)

data class FollowableProfileItem(
    val profile: AdvancedProfileView,
    val onUpdate: (Cmd) -> Unit,
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
    onOpen = { onUpdate(OpenNProfile(Nip19Profile(profile.pubkey))) }
)

data class FollowableTopicItem(
    val topic: Topic,
    val isFollowed: Boolean,
    val onUpdate: (Cmd) -> Unit,
) : FollowableItem(
    label = topic,
    icon = { Icon(imageVector = HashtagIcon, contentDescription = null) },
    button = {
        FollowButton(
            isFollowed = isFollowed,
            onFollow = { onUpdate(FollowTopic(topic)) },
            onUnfollow = { onUpdate(UnfollowTopic(topic)) },
        )
    },
    onOpen = { onUpdate(OpenTopic(topic)) }
)
