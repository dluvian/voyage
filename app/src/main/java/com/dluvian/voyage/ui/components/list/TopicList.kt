package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.theme.HashtagIcon

@Composable
fun TopicList(
    topics: List<Topic>,
    state: LazyListState,
    isRemovable: Boolean = false,
    firstRow: ComposableContent = {},
    onRemove: (Int) -> Unit = {},
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
        items = mappedTopics,
        state = state,
        isRemovable = isRemovable,
        firstRow = firstRow,
        onRemove = onRemove
    )
}
