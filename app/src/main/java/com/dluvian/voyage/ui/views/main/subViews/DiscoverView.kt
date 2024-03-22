package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DiscoverViewFollowProfile
import com.dluvian.voyage.core.DiscoverViewFollowTopic
import com.dluvian.voyage.core.DiscoverViewInit
import com.dluvian.voyage.core.DiscoverViewRefresh
import com.dluvian.voyage.core.DiscoverViewUnfollowProfile
import com.dluvian.voyage.core.DiscoverViewUnfollowTopic
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.SectionHeader
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.HashtagIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun DiscoverView(vm: DiscoverViewModel, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val topics by vm.popularTopics
    val profiles by vm.popularProfiles

    LaunchedEffect(key1 = Unit) {
        onUpdate(DiscoverViewInit)
    }

    val followableTopics = remember(topics) {
        topics.map {
            Followable(
                imageVector = HashtagIcon,
                label = it.topic,
                isFollowed = it.isFollowed,
                onFollow = { onUpdate(DiscoverViewFollowTopic(topic = it.topic)) },
                onUnfollow = { onUpdate(DiscoverViewUnfollowTopic(topic = it.topic)) },
                onOpen = { onUpdate(OpenTopic(topic = it.topic)) }
            )
        }
    }

    val followableProfiles = remember(profiles) {
        profiles.map {
            Followable(
                imageVector = AccountIcon,
                label = it.advancedProfile.name,
                isFollowed = it.advancedProfile.isFriend,
                onFollow = { onUpdate(DiscoverViewFollowProfile(pubkey = it.advancedProfile.pubkey)) },
                onUnfollow = { onUpdate(DiscoverViewUnfollowProfile(pubkey = it.advancedProfile.pubkey)) },
                onOpen = { onUpdate(OpenProfile(nip19 = it.advancedProfile.toNip19())) }
            )
        }
    }

    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(DiscoverViewRefresh) }) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { SectionHeader(header = stringResource(id = R.string.popular_topics)) }
            item {
                DiscoverContainer(
                    modifier = Modifier.fillParentMaxHeight(0.3f),
                    items = followableTopics
                )
            }
            item { SectionHeader(header = stringResource(id = R.string.popular_profiles)) }
            item {
                DiscoverContainer(
                    modifier = Modifier.fillParentMaxHeight(0.3f),
                    items = followableProfiles
                )
            }
        }
    }
}

private data class Followable(
    val imageVector: ImageVector,
    val label: String,
    val isFollowed: Boolean,
    val onFollow: Fn,
    val onUnfollow: Fn,
    val onOpen: Fn
)

@Composable
private fun DiscoverContainer(modifier: Modifier = Modifier, items: List<Followable>) {
    Box(modifier = modifier) {
        LazyHorizontalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            rows = StaggeredGridCells.Adaptive(minSize = ButtonDefaults.MinHeight),
            verticalArrangement = Arrangement.Center
        ) {
            items(items) { item ->
                DiscoverChip(item = item)
            }
        }
    }
}

@Composable
private fun DiscoverChip(item: Followable) {
    Row(
        modifier = Modifier
            .padding(spacing.medium)
            .clip(ButtonDefaults.outlinedShape)
            .clickable(onClick = item.onOpen)
            .border(
                width = 1.dp,
                shape = ButtonDefaults.outlinedShape,
                color = MaterialTheme.colorScheme.onBackground
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            modifier = Modifier.padding(start = spacing.large),
            imageVector = item.imageVector,
            contentDescription = item.label
        )
        Text(
            modifier = Modifier.padding(horizontal = spacing.large),
            text = item.label,
            style = MaterialTheme.typography.labelLarge
        )
        FollowButton(
            isFollowed = item.isFollowed,
            onFollow = item.onFollow,
            onUnfollow = item.onUnfollow
        )
    }
}
