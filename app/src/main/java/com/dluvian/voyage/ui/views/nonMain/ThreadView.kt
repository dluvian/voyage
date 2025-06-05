package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.isReply
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.OpenThreadNevent
import com.dluvian.voyage.model.ThreadReplyCtx
import com.dluvian.voyage.model.ThreadRootCtx
import com.dluvian.voyage.model.ThreadViewRefresh
import com.dluvian.voyage.parentId
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.uiEvent.UIEventRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.ThreadViewModel
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Nip19Event


@Composable
fun ThreadView(vm: ThreadViewModel, snackbar: SnackbarHostState, onUpdate: (Cmd) -> Unit) {
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.thread),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        vm.root.value.let { root ->
            if (root == null) FullLinearProgressIndicator()
            root?.let {
                ThreadViewContent(
                    root = it,
                    replies = vm.replies.value,
                    parentIsAvailable = vm.parentIsAvailable.value,
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
    root: ThreadRootCtx,
    replies: List<ThreadReplyCtx>,
    parentIsAvailable: Boolean,
    isRefreshing: Boolean,
    state: LazyListState,
    onUpdate: (Cmd) -> Unit
) {
    val adjustedReplies = remember(root, replies) {
        when (root.uiEvent.event.isReply()) {
            true -> replies.map { it.copy(level = it.level + 2) }
            false -> replies
        }
    }
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ThreadViewRefresh) }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            state = state
        ) {
            if (parentIsAvailable && root.uiEvent.event.isReply()) {
                val parentId = root.uiEvent.event.parentId()
                if (parentId != null) item {
                    OpenParentButton(
                        modifier = Modifier.padding(start = spacing.medium),
                        parentId = parentId,
                        onUpdate = onUpdate
                    )
                }
            }
            item {
                UIEventRow(
                    ctx = root,
                    onUpdate = onUpdate
                )
            }
            if (!root.uiEvent.event.isReply()) item {
                FullHorizontalDivider()
            }
            itemsIndexed(adjustedReplies) { i, reply ->
                UIEventRow(
                    ctx = reply,
                    onUpdate = onUpdate
                )
                if (i == adjustedReplies.size - 1) FullHorizontalDivider()
            }
            if (replies.isEmpty()) item {
                Column(modifier = Modifier.fillParentMaxHeight(0.5f)) {
                    BaseHint(text = stringResource(id = R.string.no_comments_found))
                }
            }
        }
    }
}

@Composable
fun MoreRepliesTextButton(onShowReplies: () -> Unit) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onShowReplies) {
            Text(text = stringResource(id = R.string.show_replies))
        }
    }
}

@Composable
private fun OpenParentButton(
    modifier: Modifier = Modifier,
    parentId: EventId,
    onUpdate: (Cmd) -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = { onUpdate(OpenThreadNevent(Nip19Event(parentId))) }
    ) {
        Text(text = stringResource(id = R.string.open_parent))
    }
}
