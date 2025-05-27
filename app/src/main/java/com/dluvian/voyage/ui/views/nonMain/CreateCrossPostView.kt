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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.GoBack
import com.dluvian.voyage.R
import com.dluvian.voyage.SendCrossPost
import com.dluvian.voyage.canAddAnotherTopic
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.components.TopicSelectionColumn
import com.dluvian.voyage.ui.components.dialog.AddTopicDialog
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.selection.NamedCheckbox
import com.dluvian.voyage.ui.theme.CrossPostIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.CreateCrossPostViewModel

@Composable
fun CreateCrossPostView(
    vm: CreateCrossPostViewModel,
    topicSuggestions: State<List<Topic>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isSending by vm.isSending
    val selectedTopics = remember { mutableStateOf(emptyList<Topic>()) }
    val isAnon = remember { mutableStateOf(false) }

    ContentCreationScaffold(
        showSendButton = false,
        isSendingContent = isSending,
        snackbar = snackbar,
        title = stringResource(
            id = R.string.cross_post_to_topics_n_of_m,
            selectedTopics.value.size, MAX_TOPICS
        ),
        onSend = { }, // We don't use top bar for sending
        onUpdate = onUpdate,
    ) {
        CreateCrossPostViewContent(
            topicSuggestions = topicSuggestions.value,
            selectedTopics = selectedTopics,
            isAnon = isAnon,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateCrossPostViewContent(
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    isAnon: MutableState<Boolean>,
    onUpdate: OnUpdate,
) {
    val showTopicSelection = remember { mutableStateOf(false) }
    if (showTopicSelection.value) AddTopicDialog(
        topicSuggestions = topicSuggestions,
        showNext = canAddAnotherTopic(selectedItemLength = selectedTopics.value.size),
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
        NamedCheckbox(
            isChecked = isAnon.value,
            name = stringResource(id = R.string.create_anonymously),
            onClick = { isAnon.value = !isAnon.value })
        CrossPostButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.xxl, horizontal = spacing.bigScreenEdge),
            selectedTopics = selectedTopics,
            isAnon = isAnon.value,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CrossPostButton(
    modifier: Modifier = Modifier,
    selectedTopics: State<List<Topic>>,
    isAnon: Boolean,
    onUpdate: OnUpdate
) {
    val context = LocalContext.current

    Button(
        modifier = modifier,
        onClick = {
            onUpdate(
                SendCrossPost(
                    topics = selectedTopics.value,
                    isAnon = isAnon,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
            )
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
