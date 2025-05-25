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
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.Poll
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.SomeReply
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.bottomSheet.PostDetailsBottomSheet
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.mainEvent.MainEventRow
import com.dluvian.voyage.ui.components.row.mainEvent.ThreadReplyCtx
import com.dluvian.voyage.ui.components.row.mainEvent.ThreadRootCtx
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
                    replies = vm.replies.value.collectAsState().value,
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
    localRoot: ThreadRootCtx,
    replies: List<ThreadReplyCtx>,
    totalReplyCount: Int,
    parentIsAvailable: Boolean,
    isRefreshing: Boolean,
    state: LazyListState,
    onUpdate: OnUpdate
) {
    val adjustedReplies = remember(localRoot, replies) {
        when (localRoot.threadableMainEvent) {
            is RootPost, is Poll -> replies
            is LegacyReply, is Comment -> replies.map { it.copy(level = it.level + 2) }
        }
    }
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ThreadViewRefresh) }) {
        val replyCountDif = remember(localRoot.mainEvent.replyCount, adjustedReplies) {
            val minLvl = adjustedReplies.minByOrNull { it.level }?.level
            localRoot.mainEvent.replyCount - adjustedReplies.filter { it.level == minLvl }.size
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            state = state
        ) {
            if (parentIsAvailable && localRoot.threadableMainEvent is SomeReply) {
                val parentId = when (localRoot.threadableMainEvent) {
                    is LegacyReply -> localRoot.threadableMainEvent.parentId
                    is Comment -> localRoot.threadableMainEvent.parentId
                }
                if (parentId != null) item {
                    OpenParentButton(
                        modifier = Modifier.padding(start = spacing.medium),
                        parentId = parentId,
                        onUpdate = onUpdate
                    )
                }
            } else if (
                !parentIsAvailable &&
                localRoot.threadableMainEvent is Comment &&
                !localRoot.threadableMainEvent.parentIsSupported()
            ) item {
                HintText(text = stringResource(id = R.string.parent_event_is_not_supported))
            }
            item {
                MainEventRow(
                    ctx = localRoot,
                    onUpdate = onUpdate
                )
            }
            when (localRoot.threadableMainEvent) {
                is RootPost, is Poll -> item { FullHorizontalDivider() }
                is Comment, is LegacyReply -> {}
            }
            if (localRoot.mainEvent.replyCount > totalReplyCount) item {
                FullLinearProgressIndicator()
            }
            itemsIndexed(adjustedReplies) { i, reply ->
                MainEventRow(
                    ctx = reply,
                    onUpdate = onUpdate
                )
                if (i == adjustedReplies.size - 1) FullHorizontalDivider()
            }

            if (replyCountDif > 0) item {
                HintText(
                    text = stringResource(
                        id = R.string.n_replies_have_been_hidden,
                        replyCountDif
                    )
                )
            }

            if (localRoot.mainEvent.replyCount == 0 && adjustedReplies.isEmpty()) item {
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
private fun OpenParentButton(
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

@Composable
private fun HintText(text: String) {
    TextButton(enabled = false, onClick = { }) {
        Text(text = text)
    }
}
