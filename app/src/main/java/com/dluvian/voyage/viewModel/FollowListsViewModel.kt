package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.FollowListsViewCmd
import com.dluvian.voyage.model.FollowListsViewRefresh
import com.dluvian.voyage.model.ShowFollowListsView
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.provider.IEventUpdate

typealias TopicFollowState = Pair<Topic, Boolean>

class FollowListsViewModel(
    val contactListState: LazyListState,
    val topicListState: LazyListState,
    val pagerState: PagerState,
) : ViewModel(), IEventUpdate {
    val tabIndex = mutableIntStateOf(0)
    val isRefreshing = mutableStateOf(false)
    val profiles = mutableStateOf(emptyList<TrustProfile>())
    val topics = mutableStateOf(emptyList<TopicFollowState>())

    fun handle(cmd: FollowListsViewCmd) {
        when (cmd) {
            ShowFollowListsView -> TODO()
            FollowListsViewRefresh -> TODO()
        }
    }
}
