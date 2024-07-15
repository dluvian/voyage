package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.components.TopicSelectionContainer
import com.dluvian.voyage.ui.components.chip.AddTopicChip
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.theme.RemoveCircleIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun TopicSelectionRow(
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    onUpdate: OnUpdate
) {
    val showDialog = remember { mutableStateOf(false) }
    TopicSelectionContainer(
        showDialog = showDialog,
        topicSuggestions = topicSuggestions,
        selectedTopics = selectedTopics,
        onUpdate = onUpdate
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.bigScreenEdge)
        ) {
            if (selectedTopics.value.size < MAX_TOPICS) item {
                AddTopicChip(onOpenTopicSelection = { showDialog.value = true })
            }
            items(selectedTopics.value) {
                TopicChip(
                    modifier = Modifier.padding(horizontal = spacing.small),
                    topic = it,
                    trailingImageVector = RemoveCircleIcon,
                    onClick = { selectedTopics.value -= it })
            }
        }
    }
}
