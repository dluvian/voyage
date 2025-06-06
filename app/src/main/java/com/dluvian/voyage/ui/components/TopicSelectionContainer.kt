package com.dluvian.voyage.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.dialog.AddTopicDialog

@Composable
fun TopicSelectionContainer(
    showDialog: MutableState<Boolean>,
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit,
) {
    if (showDialog.value) AddTopicDialog(
        topicSuggestions = topicSuggestions,
        onAdd = { topic ->
            if (!selectedTopics.value.contains(topic)) {
                selectedTopics.value += topic
            }
        },
        onDismiss = { showDialog.value = false },
        onUpdate = onUpdate
    )
    content()
}
