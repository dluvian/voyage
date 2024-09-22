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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.model.ThreadPseudoRootUI
import com.dluvian.voyage.core.model.ThreadRootItemUI
import com.dluvian.voyage.core.model.ThreadRootUI
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.bottomSheet.PostDetailsBottomSheet
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.mainEvent.old.ThreadReplyRow
import com.dluvian.voyage.ui.components.row.mainEvent.old.ThreadRootRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun ThreadView(vm: ThreadViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.thread),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        vm.localRoot.collectAsState().value.let { localRoot ->
            if (localRoot == null) FullLinearProgressIndicator()
            vm.postDetails.value?.let { details ->
                PostDetailsBottomSheet(postDetails = details, onUpdate = onUpdate)
            }
            localRoot?.let {
                ThreadViewContent(
                    localRoot = it,
                    replies = vm.leveledReplies.value.collectAsState().value,
                    totalReplyCount = vm.totalReplyCount.value.collectAsState().value,
                    parentIsAvailable = vm.parentIsAvailable.collectAsState().value,
                    isRefreshing = vm.isRefreshing.value,
                    state = vm.threadState,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadViewContent(
    localRoot: ThreadRootItemUI,
    replies: List<LeveledReplyUI>,
    totalReplyCount: Int,
    parentIsAvailable: Boolean,
    isRefreshing: Boolean,
    state: LazyListState,
    onUpdate: OnUpdate
) {
    val adjustedReplies = remember(localRoot, replies) {
        when (localRoot) {
            is ThreadRootUI -> replies
            is ThreadPseudoRootUI -> replies.map { it.copy(level = it.level + 2) }
        }
    }
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ThreadViewRefresh) }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            state = state
        ) {
            if (parentIsAvailable && localRoot is ThreadPseudoRootUI) item {
                OpenRootButton(
                    modifier = Modifier.padding(start = spacing.medium),
                    parentId = localRoot.legacyReply.parentId,
                    onUpdate = onUpdate
                )
            }
            item {
                when (localRoot) {
                    is ThreadRootUI -> {
                        ThreadRootRow(post = localRoot.rootPostUI, isOp = true, onUpdate = onUpdate)
                        FullHorizontalDivider()
                    }

                    is ThreadPseudoRootUI -> {
                        ThreadReplyRow(
                            leveledReply = LeveledReplyUI(
                                level = 1,
                                reply = localRoot.legacyReply,
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
            if (localRoot.replyCount > totalReplyCount) item {
                FullLinearProgressIndicator()
            }
            itemsIndexed(adjustedReplies) { i, leveledReply ->
                if (leveledReply.level == 0) FullHorizontalDivider()
                ThreadReplyRow(
                    leveledReply = leveledReply,
                    isLocalRoot = false, // TODO: create ThreadReplyItemUI
                    isOp = localRoot.pubkey == leveledReply.reply.pubkey,
                    onUpdate = onUpdate
                )
                if (i == adjustedReplies.size - 1) FullHorizontalDivider()
            }
            if (localRoot.replyCount == 0 && adjustedReplies.isEmpty()) item {
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
