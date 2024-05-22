package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nostr_kt.createNevent
import com.dluvian.voyage.R
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.ReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.post.ThreadReplyRow
import com.dluvian.voyage.ui.components.row.post.ThreadRootRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ThreadView(vm: ThreadViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val localRoot by vm.localRoot.collectAsState()
    val leveledReplies by vm.leveledReplies.value.collectAsState()
    val parentIsAvailable by vm.parentIsAvailable.collectAsState()

    SimpleGoBackScaffold(
        header = stringResource(id = R.string.thread),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        if (localRoot == null) FullLinearProgressIndicator()
        localRoot?.let {
            ThreadViewContent(
                localRoot = it,
                leveledReplies = leveledReplies,
                parentIsAvailable = parentIsAvailable,
                isRefreshing = isRefreshing,
                state = vm.threadState,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
private fun ThreadViewContent(
    localRoot: ParentUI,
    leveledReplies: List<LeveledReplyUI>,
    parentIsAvailable: Boolean,
    isRefreshing: Boolean,
    state: LazyListState,
    onUpdate: OnUpdate
) {
    val replies = remember(localRoot, leveledReplies) {
        when (localRoot) {
            is ReplyUI -> leveledReplies.map { it.copy(level = it.level + 2) }
            is RootPostUI -> leveledReplies
        }
    }
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ThreadViewRefresh) }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            state = state
        ) {
            if (parentIsAvailable && localRoot is ReplyUI) item {
                OpenRootButton(
                    modifier = Modifier.padding(start = spacing.medium),
                    parentId = localRoot.parentId,
                    onUpdate = onUpdate
                )
            }
            item {
                when (localRoot) {
                    is RootPostUI -> {
                        ThreadRootRow(post = localRoot, isOp = true, onUpdate = onUpdate)
                        FullHorizontalDivider()
                    }

                    is ReplyUI -> {
                        ThreadReplyRow(
                            leveledReply = LeveledReplyUI(
                                level = 1,
                                reply = localRoot,
                                isCollapsed = false,
                                hasLoadedReplies = true,
                            ),
                            isLocalRoot = true,
                            isCollapsed = false,
                            isOp = true,
                            onUpdate = onUpdate,
                        )
                    }
                }
            }
            if (localRoot.replyCount > replies.size) item {
                FullLinearProgressIndicator()
            }
            itemsIndexed(replies) { i, reply ->
                if (reply.level == 0) FullHorizontalDivider()
                ThreadReplyRow(
                    leveledReply = reply,
                    isLocalRoot = false,
                    isOp = localRoot.pubkey == reply.reply.pubkey,
                    onUpdate = onUpdate
                )
                if (i == replies.size - 1) FullHorizontalDivider()
            }
            if (localRoot.replyCount == 0 && replies.isEmpty()) item {
                Column(modifier = Modifier.fillParentMaxHeight(0.5f)) {
                    BaseHint(text = stringResource(id = R.string.no_comments_found))
                }
            }
        }
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
            else Text(text = "$replyCount ${stringResource(id = R.string.replies_lowercase)}")
        }
        if (isLoading.value) CircularProgressIndicator(
            modifier = Modifier.size(sizing.smallIndicator),
            strokeWidth = sizing.smallIndicatorStrokeWidth
        )
    }
}

@Composable
private fun OpenRootButton(
    modifier: Modifier = Modifier,
    parentId: EventIdHex,
    onUpdate: OnUpdate
) {
    TextButton(
        modifier = modifier,
        onClick = { onUpdate(OpenThreadRaw(nevent = createNevent(hex = parentId))) }
    ) {
        Text(text = stringResource(id = R.string.open_parent))
    }
}
