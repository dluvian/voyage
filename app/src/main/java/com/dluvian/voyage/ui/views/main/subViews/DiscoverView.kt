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
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.DiscoverViewInit
import com.dluvian.voyage.core.DiscoverViewRefresh
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.icon.AccountIconWithBadge
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.theme.HashtagIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun DiscoverView(vm: DiscoverViewModel, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val topics by vm.popularTopics.value.collectAsState()
    val profiles by vm.popularProfiles.value.collectAsState()

    val topicState = rememberLazyStaggeredGridState()
    val profileState = rememberLazyStaggeredGridState()

    LaunchedEffect(key1 = Unit) {
        onUpdate(DiscoverViewInit)
    }
    LaunchedEffect(key1 = isRefreshing) {
        if (isRefreshing) {
            topicState.scrollToItem(0)
            profileState.scrollToItem(0)
        }
    }
    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)

    val followableTopics = remember(topics) {
        topics.map {
            Followable(
                label = it.topic,
                isFollowed = it.isFollowed,
                icon = {
                    Icon(
                        modifier = Modifier.padding(start = spacing.large),
                        imageVector = HashtagIcon,
                        contentDescription = it.topic
                    )
                },
                onFollow = {
                    onUpdate(FollowTopic(topic = it.topic, signerLauncher = signerLauncher))
                },
                onUnfollow = {
                    onUpdate(UnfollowTopic(topic = it.topic, signerLauncher = signerLauncher))
                }
            ) { onUpdate(OpenTopic(topic = it.topic)) }
        }
    }

    val followableProfiles = remember(profiles) {
        profiles.map {
            Followable(
                label = it.inner.name,
                isFollowed = it.inner.isFriend,
                icon = {
                    AccountIconWithBadge(
                        trustType = TrustType.from(
                            isOneself = it.inner.isMe,
                            isFriend = it.inner.isFriend,
                            isWebOfTrust = it.inner.isWebOfTrust
                        ),
                        description = it.inner.name,
                    )
                },
                onFollow = {
                    onUpdate(
                        FollowProfile(pubkey = it.inner.pubkey, signerLauncher = signerLauncher)
                    )
                },
                onUnfollow = {
                    onUpdate(
                        UnfollowProfile(pubkey = it.inner.pubkey, signerLauncher = signerLauncher)
                    )
                }
            ) { onUpdate(OpenProfile(nprofile = it.inner.toNip19())) }
        }
    }

    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(DiscoverViewRefresh) }) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { SectionHeader(header = stringResource(id = R.string.popular_topics)) }
            item {
                DiscoverContainer(
                    modifier = Modifier.fillParentMaxHeight(0.3f),
                    items = followableTopics,
                    hintIfEmpty = stringResource(id = R.string.no_topics_found),
                    state = topicState
                )
            }
            item { SectionHeader(header = stringResource(id = R.string.popular_profiles)) }
            item {
                DiscoverContainer(
                    modifier = Modifier.fillParentMaxHeight(0.3f),
                    items = followableProfiles,
                    hintIfEmpty = stringResource(id = R.string.no_profiles_found),
                    state = profileState
                )
            }
        }
    }
}

private data class Followable(
    val label: String,
    val isFollowed: Boolean,
    val icon: ComposableContent,
    val onFollow: Fn,
    val onUnfollow: Fn,
    val onOpen: Fn
)

@Composable
private fun DiscoverContainer(
    modifier: Modifier = Modifier,
    items: List<Followable>,
    hintIfEmpty: String,
    state: LazyStaggeredGridState,
) {
    Box(modifier = modifier) {
        if (items.isEmpty()) BaseHint(text = hintIfEmpty)
        else LazyHorizontalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            state = state,
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
        item.icon()
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
