package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.DiscoverViewInit
import com.dluvian.voyage.DiscoverViewRefresh
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.chip.FollowChip
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.model.FollowableItem
import com.dluvian.voyage.ui.model.FollowableProfileItem
import com.dluvian.voyage.ui.model.FollowableTopicItem
import com.dluvian.voyage.viewModel.DiscoverViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            FollowableTopicItem(topic = it.topic, isFollowed = it.isFollowed, onUpdate = onUpdate)
        }
    }
    val followableProfiles = remember(profiles) {
        profiles.map { FollowableProfileItem(profile = it, onUpdate = onUpdate) }
    }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(DiscoverViewRefresh) }) {
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
    items: List<FollowableItem>,
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
