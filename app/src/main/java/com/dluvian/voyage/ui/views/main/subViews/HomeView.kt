package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.core.ClickComment
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.RefreshHomeView
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.ui.components.CommentButton
import com.dluvian.voyage.ui.components.EdgeToEdgeColWithDivider
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.RelativeTime
import com.dluvian.voyage.ui.components.TopicChip
import com.dluvian.voyage.ui.components.VoteBox
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun HomeView(posts: List<RootPost>, isRefreshing: Boolean, onUpdate: OnUpdate) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(RefreshHomeView) }) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(posts) { post ->
                PostRow(post = post, onUpdate = onUpdate)
            }
        }
    }
}

@Composable
private fun PostRow(post: RootPost, onUpdate: OnUpdate) {
    EdgeToEdgeColWithDivider(verticalPadding = spacing.screenEdge) {
        Header(
            topic = post.topic,
            time = post.time
        )
        Spacer(modifier = Modifier.height(spacing.medium))
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
        Actions(
            post = post,
            onUpdate = onUpdate,
        )
    }
}

@Composable
private fun Header(topic: String, time: Long) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TopicChip(modifier = Modifier.weight(weight = 1f, fill = false), topic = topic)
        Spacer(modifier = Modifier.width(spacing.large))
        RelativeTime(from = time)
    }
}

@Composable
private fun Actions(
    post: RootPost,
    onUpdate: OnUpdate
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        VoteBox(
            postId = post.id,
            authorPubkey = post.pubkey,
            myVote = post.myVote,
            tally = post.tally,
            ratioInPercent = post.ratioInPercent,
            onUpdate = onUpdate
        )
        CommentButton(
            commentCount = post.commentCount,
            onClick = { onUpdate(ClickComment(postId = post.id)) })
    }
}
