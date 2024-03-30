package com.dluvian.voyage.ui.views.nonMain.thread

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.LeveledCommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.ui.components.CommentRow
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.PostRow
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ThreadView(vm: ThreadViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val root by vm.root.collectAsState()
    val leveledComments by vm.leveledComments.value.collectAsState()

    ThreadScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        if (root == null) FullLinearProgressIndicator()
        root?.let {
            ThreadViewContent(
                root = it,
                leveledComments = leveledComments,
                isRefreshing = isRefreshing,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
private fun ThreadViewContent(
    root: RootPostUI,
    leveledComments: List<LeveledCommentUI>,
    isRefreshing: Boolean,
    onUpdate: OnUpdate
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ThreadViewRefresh) }) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                PostRow(post = root, isDetailed = true, onUpdate = onUpdate)
                FullHorizontalDivider()
            }
            if (root.commentCount > leveledComments.size) item {
                FullLinearProgressIndicator()
            }
            itemsIndexed(leveledComments) { i, comment ->
                if (comment.level == 0) FullHorizontalDivider()
                Comment(
                    leveledComment = comment,
                    onUpdate = onUpdate
                )
                if (i == leveledComments.size - 1) FullHorizontalDivider()
            }
            if (leveledComments.isEmpty()) item {
                Column(modifier = Modifier.fillParentMaxHeight(0.5f)) {
                    BaseHint(text = stringResource(id = R.string.no_comments_found))
                }
            }
        }
    }
}

@Composable
private fun Comment(
    leveledComment: LeveledCommentUI,
    onUpdate: OnUpdate
) {
    RowWithDivider(level = leveledComment.level) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdate(ThreadViewToggleCollapse(id = leveledComment.comment.id)) }) {
            CommentRow(
                comment = leveledComment.comment,
                isCollapsed = leveledComment.isCollapsed,
                onUpdate = onUpdate
            )
            if (leveledComment.comment.commentCount > 0 &&
                !leveledComment.isCollapsed &&
                !leveledComment.hasLoadedReplies
            ) MoreRepliesTextButton(
                commentCount = leveledComment.comment.commentCount,
                onShowReplies = { onUpdate(ThreadViewShowReplies(id = leveledComment.comment.id)) })
        }
    }
}

@Composable
fun RowWithDivider(level: Int, content: ComposableContent) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        repeat(times = level) {
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = spacing.xl, end = spacing.medium)
            )
        }
        content()
    }
}

@Composable
fun MoreRepliesTextButton(commentCount: Int, onShowReplies: Fn) {
    TextButton(onClick = onShowReplies) {
        if (commentCount == 1) Text(text = stringResource(id = R.string.one_reply))
        else Text(text = "$commentCount ${stringResource(id = R.string.replies)}")
    }
}
