package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.core.ClickDownvote
import com.dluvian.voyage.core.ClickUpvote
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.RefreshHomeView
import com.dluvian.voyage.core.model.Downvote
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.Upvote
import com.dluvian.voyage.ui.components.EdgeToEdgeColWithDivider
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.TopicChip
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
            timeStr = post.timeStr
        )
        Spacer(modifier = Modifier.height(spacing.small))
        if (post.title.isNotEmpty()) Text(
            text = post.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.content,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Actions(
            post = post,
            onUpdate = onUpdate,
        )
    }
}

@Composable
private fun Header(topic: String, timeStr: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TopicChip(topic = topic)
        Spacer(modifier = Modifier.width(6.dp)) // TODO: Define spacing in different file
        Text(text = timeStr)
    }
}

@Composable
private fun Actions(
    post: RootPost,
    onUpdate: OnUpdate
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            Icon(
                modifier = Modifier.clickable {
                    onUpdate(
                        ClickUpvote(
                            postId = post.id,
                            pubkey = post.pubkey
                        )
                    )
                },
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = if (post.myVote is Upvote) Color.Red else Color.Unspecified
            )
            Text(text = "${post.tally} (${post.ratioInPercent}%)")
            Icon(
                modifier = Modifier.clickable {
                    onUpdate(
                        ClickDownvote(
                            postId = post.id,
                            pubkey = post.pubkey
                        )
                    )
                },
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (post.myVote is Downvote) Color.Blue else Color.Unspecified
            )
        }
        Text(text = "${post.commentCount} Comments")
    }
}
