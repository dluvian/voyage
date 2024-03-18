package com.dluvian.voyage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.ClickThread
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.IPaginator
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.ui.components.chip.CommentChip
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun Feed(
    paginator: IPaginator,
    onRefresh: Fn,
    onAppend: Fn,
    onUpdate: OnUpdate
) {
    val isRefreshing by paginator.isRefreshing
    val isAppending by paginator.isAppending
    val postsOuterState by paginator.page
    val posts by postsOuterState.collectAsState()

    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        if (isAppending) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(items = posts, key = { _, item -> item.id }) { i, post ->
                PostRow(
                    post = post,
                    onUpdate = onUpdate
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = spacing.tiny)
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

@Composable
private fun PostRow(post: RootPost, onUpdate: OnUpdate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onUpdate(ClickThread(postId = post.id)) })
            .padding(spacing.screenEdge)
    ) {
        Header(post = post)
        Spacer(modifier = Modifier.height(spacing.large))
        if (post.title.isNotEmpty()) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(spacing.large))
        }
        Text(
            text = post.content,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(spacing.large))
        Actions(post = post, onUpdate = onUpdate)
    }
}

@Composable
private fun Header(post: RootPost) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TrustIndicator(trustType = post.trustType)
        post.topic?.let { topic ->
            TopicChip(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .padding(start = spacing.large),
                topic = topic
            )
        }
        Spacer(modifier = Modifier.width(spacing.large))
        RelativeTime(from = post.createdAt)
    }
}

@Composable
private fun Actions(
    post: RootPost,
    onUpdate: OnUpdate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VoteBox(
            postId = post.id,
            authorPubkey = post.pubkey,
            myVote = post.myVote,
            tally = post.tally,
            onUpdate = onUpdate
        )
        CommentChip(
            commentCount = post.commentCount,
            onClick = { onUpdate(ClickThread(postId = post.id)) })
    }
}
