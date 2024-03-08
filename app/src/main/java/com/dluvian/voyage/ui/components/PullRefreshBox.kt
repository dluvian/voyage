package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.dluvian.voyage.core.ComposableContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshBox(isRefreshing: Boolean, onRefresh: () -> Unit, content: ComposableContent) {
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            if (!isRefreshing) onRefresh()
        }
    }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) pullRefreshState.startRefresh()
        else pullRefreshState.endRefresh()
    }
    Box(Modifier.nestedScroll(pullRefreshState.nestedScrollConnection)) {
        content()
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState,
            containerColor = PullToRefreshDefaults.containerColor,
            contentColor = PullToRefreshDefaults.contentColor
        )
    }
}
