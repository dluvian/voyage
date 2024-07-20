package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.rememberCoroutineScope
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.model.FollowableOrMutableItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileAndTopicList(
    isRefreshing: Boolean,
    headers: List<String>,
    profiles: List<FollowableOrMutableItem>,
    topics: List<FollowableOrMutableItem>,
    profileState: LazyListState,
    topicState: LazyListState,
    tabIndex: MutableIntState,
    pagerState: PagerState,
    onRefresh: Fn,
) {
    val scope = rememberCoroutineScope()
    SimpleTabPager(
        headers = headers,
        index = tabIndex,
        pagerState = pagerState,
        onScrollUp = {
            when (it) {
                0 -> scope.launch { profileState.animateScrollToItem(0) }
                1 -> scope.launch { topicState.animateScrollToItem(0) }
                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> FollowOrMuteList(
                rows = profiles,
                isRefreshing = isRefreshing,
                state = profileState,
                onRefresh = onRefresh
            )

            1 -> FollowOrMuteList(
                rows = topics,
                isRefreshing = isRefreshing,
                state = topicState,
                onRefresh = onRefresh
            )

            else -> ComingSoon()
        }
    }
}
