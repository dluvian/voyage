package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class FollowListsViewModel @OptIn(ExperimentalFoundationApi::class) constructor(
    val contactListState: LazyListState,
    val topicListState: LazyListState,
    val pagerState: PagerState,
) : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
}
