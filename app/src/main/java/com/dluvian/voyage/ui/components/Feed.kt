package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.IPaginator

@Composable
fun Feed(
    paginator: IPaginator,
    onRefresh: Fn,
    onAppend: Fn,
    onUpdate: OnUpdate,
    header: ComposableContent = {},
) {
    val isRefreshing by paginator.isRefreshing
    val isAppending by paginator.isAppending
    val posts by paginator.page.value.collectAsState()

    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        if (isAppending) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { header() }
            items(items = posts, key = { item -> item.id }) { post ->
                PostRow(
                    post = post,
                    onUpdate = onUpdate
                )
                FullHorizontalDivider()
            }
            if (posts.isNotEmpty()) {
                item {
                    Button(onClick = onAppend) {
                        Text(text = "Next Page") // TODO: Make it look good
                    }
                }
            }
        }
    }
}
