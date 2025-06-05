package com.dluvian.voyage.ui.views.nonMain


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.MAX_TOPICS
import com.dluvian.voyage.R
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.SendCrossPost
import com.dluvian.voyage.ui.components.TopicSelectionColumn
import com.dluvian.voyage.ui.components.dialog.AddTopicDialog
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.theme.CrossPostIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.CrossPostViewModel
import rust.nostr.sdk.Event

@Composable
fun CrossPostView(
    vm: CrossPostViewModel,
    topicSuggestions: State<List<Topic>>,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    ContentCreationScaffold(
        showSendButton = false,
        snackbar = snackbar,
        title = stringResource(
            id = R.string.cross_post_to_topics_n_of_m,
            vm.topics.value.size, MAX_TOPICS
        ),
        onSend = { }, // We don't use top bar for sending
        onUpdate = onUpdate,
    ) {
        vm.event.value?.let { event ->
            CreateCrossPostViewContent(
                topicSuggestions = topicSuggestions.value,
                selectedTopics = vm.topics,
                event = event,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
private fun CreateCrossPostViewContent(
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    event: Event,
    onUpdate: (Cmd) -> Unit,
) {
    val showTopicSelection = remember { mutableStateOf(false) }
    if (showTopicSelection.value) AddTopicDialog(
        topicSuggestions = topicSuggestions,
        onAdd = { topic -> selectedTopics.value += topic },
        onDismiss = { showTopicSelection.value = false },
        onUpdate = onUpdate
    )

    Column {
        TopicSelectionColumn(
            modifier = Modifier.weight(1f, fill = false),
            topicSuggestions = topicSuggestions,
            selectedTopics = selectedTopics,
            onUpdate = onUpdate
        )
        CrossPostButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.xxl, horizontal = spacing.bigScreenEdge),
            selectedTopics = selectedTopics,
            event = event,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CrossPostButton(
    modifier: Modifier = Modifier,
    selectedTopics: State<List<Topic>>,
    event: Event,
    onUpdate: (Cmd) -> Unit
) {
    Button(
        modifier = modifier,
        onClick = {
            onUpdate(SendCrossPost(selectedTopics.value, event))
        }) {
        Icon(
            modifier = Modifier.size(sizing.smallIndicator),
            imageVector = CrossPostIcon,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(spacing.small))
        if (selectedTopics.value.isEmpty()) {
            Text(text = stringResource(id = R.string.cross_post_without_topics))
        } else {
            Text(
                text = stringResource(
                    id = R.string.cross_post_to_n_topics,
                    selectedTopics.value.size
                )
            )
        }
    }
}
