package com.dluvian.voyage.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.components.list.TopicList
import com.dluvian.voyage.ui.components.row.AddRow


@Composable
fun TopicSelectionColumn(
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    modifier: Modifier = Modifier,
    onUpdate: OnUpdate
) {
    val showDialog = remember { mutableStateOf(false) }
    TopicSelectionContainer(
        showDialog = showDialog,
        topicSuggestions = topicSuggestions,
        selectedTopics = selectedTopics,
        onUpdate = onUpdate
    ) {
        TopicList(
            modifier = modifier,
            topics = selectedTopics.value,
            isRemovable = true,
            lastRow = {
                if (selectedTopics.value.size < MAX_TOPICS) AddRow(
                    header = stringResource(id = R.string.add_topic),
                    onClick = { showDialog.value = true })
            },
            onRemove = { i ->
                selectedTopics.value = selectedTopics.value
                    .toMutableList()
                    .apply { removeAt(i) }
            }
        )
    }
}
