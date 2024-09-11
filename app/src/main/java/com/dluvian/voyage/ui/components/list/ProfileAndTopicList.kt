package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.MuteWord
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.UnmuteWord
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.dialog.AddMuteWordDialog
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.row.AddRow
import com.dluvian.voyage.ui.model.FollowableOrMutableItem
import com.dluvian.voyage.ui.theme.WordIcon
import com.dluvian.voyage.ui.theme.getTrustColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileAndTopicList(
    isRefreshing: Boolean,
    headers: List<String>,
    profiles: List<FollowableOrMutableItem>,
    topics: List<FollowableOrMutableItem>,
    profileState: LazyListState,
    topicState: LazyListState,
    tabIndex: MutableIntState,
    pagerState: PagerState,
    words: MutableState<List<String>>? = null,
    wordState: LazyListState? = null,
    onRefresh: Fn,
    onUpdate: OnUpdate,
) {
    val scope = rememberCoroutineScope()
    SimpleTabPager(
        headers = headers,
        index = tabIndex,
        pagerState = pagerState,
        onScrollUp = {
            when (it) {
                0 -> scope.launch { profileState.animateScrollToItem(0) }
                1 -> scope.launch { topicState.animateScrollToItem(0) }
                2 -> if (words != null && wordState != null) {
                    scope.launch { wordState.animateScrollToItem(0) }
                }

                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> FollowOrMuteList(
                rows = profiles,
                isRefreshing = isRefreshing,
                state = profileState,
                onRefresh = onRefresh
            )

            1 -> FollowOrMuteList(
                rows = topics,
                isRefreshing = isRefreshing,
                state = topicState,
                onRefresh = onRefresh
            )

            2 -> if (words != null && wordState != null) {
                WordList(
                    words = words,
                    totalCount = remember(words, profiles, topics) {
                        words.value.size + profiles.size + topics.size
                    },
                    state = wordState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onUpdate = onUpdate
                )
            } else ComingSoon()

            else -> ComingSoon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordList(
    words: MutableState<List<String>>,
    totalCount: Int,
    state: LazyListState,
    isRefreshing: Boolean,
    onRefresh: Fn,
    onUpdate: OnUpdate
) {
    val showAddWordDialog = remember { mutableStateOf(false) }

    if (showAddWordDialog.value) {
        AddMuteWordDialog(
            showNext = totalCount < MAX_KEYS_SQL,
            onAdd = { word ->
                words.value = words.value.let { currentList ->
                    if (currentList.contains(word)) {
                        currentList
                    } else {
                        onUpdate(MuteWord(word = word, debounce = false))
                        currentList + word
                    }
                }
            },
            onDismiss = { showAddWordDialog.value = false },
        )
    }
    val mappedWords = remember(words.value) {
        words.value.map { word ->
            ItemProps(
                first = {
                    Icon(
                        imageVector = WordIcon,
                        tint = getTrustColor(trustType = Muted),
                        contentDescription = null
                    )
                },
                second = word,
            )
        }
    }
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        ItemList(
            items = mappedWords,
            state = state,
            isRemovable = true,
            firstRow = {
                if (totalCount < MAX_KEYS_SQL) AddRow(
                    header = stringResource(id = R.string.add_word),
                    onClick = { showAddWordDialog.value = true })
            },
            onRemove = { i ->
                onUpdate(UnmuteWord(word = words.value[i], debounce = true))
                words.value = words.value
                    .toMutableList()
                    .apply { removeAt(i) }
            },
        )
    }
}
