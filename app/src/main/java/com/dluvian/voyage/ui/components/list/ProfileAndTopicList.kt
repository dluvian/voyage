package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.model.FollowableItem
import kotlinx.coroutines.launch

@Composable
fun ProfileAndTopicList(
    isRefreshing: Boolean,
    headers: List<String>,
    profiles: List<FollowableItem>,
    topics: List<FollowableItem>,
    profileState: LazyListState,
    topicState: LazyListState,
    tabIndex: MutableIntState,
    pagerState: PagerState,
    words: MutableState<List<String>>? = null,
    wordState: LazyListState? = null,
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
                2 -> if (words != null && wordState != null) {
                    scope.launch { wordState.animateScrollToItem(0) }
                }

                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> FollowList(
                rows = profiles,
                isRefreshing = isRefreshing,
                state = profileState,
                onRefresh = onRefresh
            )

            1 -> FollowList(
                rows = topics,
                isRefreshing = isRefreshing,
                state = topicState,
                onRefresh = onRefresh
            )

            else -> ComingSoon()
        }
    }
}
