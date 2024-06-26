package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DiscoverViewInit
import com.dluvian.voyage.core.DiscoverViewRefresh
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.chip.FollowChip
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.model.Followable
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
                onFollow = { onUpdate(FollowTopic(topic = it.topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic = it.topic)) },
                onOpen = { onUpdate(OpenTopic(topic = it.topic)) },
            )
        }
    }

    val followableProfiles = remember(profiles) {
        profiles.map {
            Followable(
                label = it.inner.name,
                isFollowed = it.inner.isFriend,
                icon = {
                    Box(
                        modifier = Modifier
                            .padding(start = spacing.large)
                            .fillMaxHeight(0.7f),
                        contentAlignment = Alignment.Center
                    ) {
                        TrustIcon(profile = it.inner)
                    }
                },
                onFollow = { onUpdate(FollowProfile(pubkey = it.inner.pubkey)) },
                onUnfollow = { onUpdate(UnfollowProfile(pubkey = it.inner.pubkey)) },
                onOpen = { onUpdate(OpenProfile(nprofile = it.inner.toNip19())) }
            )
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
                FollowChip(item = item)
            }
        }
    }
}
