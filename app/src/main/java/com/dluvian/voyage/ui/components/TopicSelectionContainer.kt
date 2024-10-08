package com.dluvian.voyage.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.utils.canAddAnotherTopic
import com.dluvian.voyage.ui.components.dialog.AddTopicDialog

@Composable
fun TopicSelectionContainer(
    showDialog: MutableState<Boolean>,
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    onUpdate: OnUpdate,
    content: ComposableContent,
) {
    if (showDialog.value) AddTopicDialog(
        topicSuggestions = topicSuggestions,
        showNext = canAddAnotherTopic(selectedItemLength = selectedTopics.value.size),
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
