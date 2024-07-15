package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.theme.HashtagIcon

@Composable
fun TopicList(
    topics: List<Topic>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    isRemovable: Boolean = false,
    firstRow: ComposableContent = {},
    lastRow: ComposableContent = {},
    onRemove: (Int) -> Unit = {},
    onClick: (Int) -> Unit = {},
) {
    val mappedTopics = remember(topics) {
        topics.map { topic ->
            ItemProps(
                first = { Icon(imageVector = HashtagIcon, contentDescription = null) },
                second = topic,
            )
        }
    }
    ItemList(
        modifier = modifier,
        items = mappedTopics,
        state = state,
        isRemovable = isRemovable,
        firstRow = firstRow,
        lastRow = lastRow,
        onRemove = onRemove,
        onClick = onClick,
    )
}
