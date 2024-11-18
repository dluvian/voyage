package com.dluvian.voyage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.FEED_PAGE_SIZE
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.IPaginator
import com.dluvian.voyage.core.utils.showScrollButton
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.ui.components.bottomSheet.PostDetailsBottomSheet
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.mainEvent.MainEventRow
import com.dluvian.voyage.ui.theme.ScrollUpIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feed(
    paginator: IPaginator,
    postDetails: State<PostDetails?>,
    state: LazyListState,
    showAuthorName: Boolean,
    onRefresh: Fn,
    onAppend: Fn,
    onUpdate: OnUpdate,
) {
    val isRefreshing by paginator.isRefreshing
    val isAppending by paginator.isAppending
    val hasMoreRecentItems by paginator.hasMoreRecentItems
    val hasPage by paginator.hasPage.value.collectAsState()
    val pageTimestamps by paginator.pageTimestamps
    val filteredPage by paginator.filteredPage.value.collectAsState()
    val scope = rememberCoroutineScope()
    val showProgressIndicator by remember {
        derivedStateOf { isAppending || (hasPage && pageTimestamps.isEmpty()) }
    }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        if (showProgressIndicator) FullLinearProgressIndicator()
        if (!hasPage && pageTimestamps.isEmpty()) BaseHint(stringResource(id = R.string.no_posts_found))
        postDetails.value?.let { details ->
            PostDetailsBottomSheet(postDetails = details, onUpdate = onUpdate)
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            if (hasMoreRecentItems) item { MostRecentPostsTextButton(onClick = onRefresh) }

            items(
                items = filteredPage,
                key = { item -> item.mainEvent.id }
            ) { mainEventCtx ->
                MainEventRow(
                    ctx = mainEventCtx,
                    showAuthorName = showAuthorName,
                    onUpdate = onUpdate
                )
                FullHorizontalDivider()
            }

            if (pageTimestamps.size >= FEED_PAGE_SIZE) item {
                NextPageButton(onAppend = onAppend)
            }
        }
        if (state.showScrollButton()) {
            ScrollUpButton(onScrollToTop = { scope.launch { state.animateScrollToItem(index = 0) } })
        }
    }
}

@Composable
private fun MostRecentPostsTextButton(onClick: Fn) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onClick) {
            Text(text = stringResource(id = R.string.click_to_load_most_recent_posts))
        }
    }
}

@Composable
private fun NextPageButton(onAppend: Fn) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            modifier = Modifier.padding(horizontal = spacing.screenEdge),
            onClick = onAppend
        ) {
            Text(text = stringResource(id = R.string.next_page))
        }
    }
}

@Composable
private fun ScrollUpButton(onScrollToTop: Fn) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = spacing.bigScreenEdge)
            .padding(bottom = spacing.bigScreenEdge)
            .padding(spacing.bigScreenEdge)
            .padding(spacing.bigScreenEdge),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onScrollToTop)
                .size(sizing.scrollUpButton),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ScrollUpIcon,
                tint = Color.Black,
                contentDescription = stringResource(id = R.string.scroll_to_the_page_top)
            )
        }
    }
}
