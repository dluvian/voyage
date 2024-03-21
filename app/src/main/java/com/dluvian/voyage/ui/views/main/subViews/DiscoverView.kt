package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.DiscoverViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.ui.components.PullRefreshBox

@Composable
fun DiscoverView(vm: DiscoverViewModel, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val topics by vm.popularTopics
    val profiles by vm.popularProfiles

    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(DiscoverViewRefresh) }) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Text(text = topics.toString()) }
            item { Text(text = profiles.toString()) }
        }
    }
}
