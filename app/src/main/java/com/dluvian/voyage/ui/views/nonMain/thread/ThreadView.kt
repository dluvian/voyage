package com.dluvian.voyage.ui.views.nonMain.thread

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.PostRow
import com.dluvian.voyage.ui.components.row.ReplyRow
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ThreadView(vm: ThreadViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val root by vm.root.collectAsState()
    val leveledReplies by vm.leveledReplies.value.collectAsState()

    ThreadScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        if (root == null) FullLinearProgressIndicator()
        root?.let {
            ThreadViewContent(
                root = it,
                leveledReplies = leveledReplies,
                isRefreshing = isRefreshing,
                state = vm.threadState,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
private fun ThreadViewContent(
    root: RootPostUI,
    leveledReplies: List<LeveledReplyUI>,
    isRefreshing: Boolean,
    state: LazyListState,
    onUpdate: OnUpdate
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ThreadViewRefresh) }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            state = state
        ) {
            item {
                PostRow(post = root, isDetailed = true, onUpdate = onUpdate)
                FullHorizontalDivider()
            }
            if (root.replyCount > leveledReplies.size) item {
                FullLinearProgressIndicator()
            }
            itemsIndexed(leveledReplies) { i, reply ->
                if (reply.level == 0) FullHorizontalDivider()
                Reply(leveledReply = reply, onUpdate = onUpdate)
                if (i == leveledReplies.size - 1) FullHorizontalDivider()
            }
            if (root.replyCount == 0 && leveledReplies.isEmpty()) item {
                Column(modifier = Modifier.fillParentMaxHeight(0.5f)) {
                    BaseHint(text = stringResource(id = R.string.no_comments_found))
                }
            }
        }
    }
}

@Composable
private fun Reply(
    leveledReply: LeveledReplyUI,
    onUpdate: OnUpdate
) {
    RowWithDivider(level = leveledReply.level) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdate(ThreadViewToggleCollapse(id = leveledReply.reply.id)) }) {
            ReplyRow(
                reply = leveledReply.reply,
                isCollapsed = leveledReply.isCollapsed,
                showDetailedReply = leveledReply.level == 0,
                isOp = leveledReply.isOp,
                onUpdate = onUpdate,
                additionalStartAction = {
                    if (leveledReply.reply.replyCount > 0 && !leveledReply.hasLoadedReplies) {
                        MoreRepliesTextButton(
                            replyCount = leveledReply.reply.replyCount,
                            onShowReplies = {
                                onUpdate(ThreadViewShowReplies(id = leveledReply.reply.id))
                            }
                        )
                    }
                }
            )
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
fun MoreRepliesTextButton(replyCount: Int, onShowReplies: Fn) {
    val isLoading = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            onShowReplies()
            isLoading.value = true
        }) {
            if (replyCount == 1) Text(text = stringResource(id = R.string.one_reply))
            else Text(text = "$replyCount ${stringResource(id = R.string.replies)}")
        }
        if (isLoading.value) CircularProgressIndicator(
            modifier = Modifier.size(sizing.smallIndicator),
            strokeWidth = sizing.smallIndicatorStrokeWidth
        )
    }
}
