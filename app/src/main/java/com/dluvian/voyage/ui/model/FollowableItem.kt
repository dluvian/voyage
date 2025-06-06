package com.dluvian.voyage.ui.model

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowTopic
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.OneselfProfile
import com.dluvian.voyage.model.OpenProfile
import com.dluvian.voyage.model.OpenTopic
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.TrustedProfile
import com.dluvian.voyage.model.UnfollowProfile
import com.dluvian.voyage.model.UnfollowTopic
import com.dluvian.voyage.model.UnknownProfile
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
    val profile: TrustProfile,
    val onUpdate: (Cmd) -> Unit,
) : FollowableItem(
    label = profile.uiName(),
    icon = { TrustIcon(profile = profile) },
    button = {
        FollowButton(
            isFollowed = when (profile) {
                is FollowedProfile -> true
                is OneselfProfile, is TrustedProfile, is UnknownProfile -> false
            },
            isEnabled = when (profile) {
                is FollowedProfile, is TrustedProfile, is UnknownProfile -> false
                is OneselfProfile -> false
            },
            onFollow = { onUpdate(FollowProfile(pubkey = profile.pubkey)) },
            onUnfollow = { onUpdate(UnfollowProfile(pubkey = profile.pubkey)) },
        )
    },
    onOpen = { onUpdate(OpenProfile(Nip19Profile(profile.pubkey))) }
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
