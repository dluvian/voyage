package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.ui.components.TextInput
import com.dluvian.voyage.ui.components.chip.SmallAssistChip
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.FancyBrush
import com.dluvian.voyage.ui.theme.RemoveCircleIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreatePostView(vm: CreatePostViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val header = remember { mutableStateOf("") }
    val body = remember { mutableStateOf("") }
    val topics = remember { mutableStateOf(emptyList<Topic>()) }
    val myTopics by vm.myTopics
    val isSendingPost by vm.isSendingPost
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
        vm.updateMyTopics()
    }

    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)

    ContentCreationScaffold(
        showSendButton = body.value.isNotBlank(),
        isSendingContent = isSendingPost,
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendPost(
                    header = header.value,
                    body = body.value,
                    topics = topics.value,
                    context = context,
                    signerLauncher = signerLauncher
                ) { onUpdate(GoBack) }
            )
        },
        onUpdate = onUpdate
    ) {
        CreatePostContent(
            header = header,
            body = body,
            topics = topics,
            myTopics = myTopics,
            focusRequester = focusRequester
        )
    }
}

@Composable
private fun CreatePostContent(
    header: MutableState<String>,
    body: MutableState<String>,
    topics: MutableState<List<Topic>>,
    myTopics: List<Topic>,
    focusRequester: FocusRequester
) {
    Column {
        TopicBar(topics = topics, myTopics = myTopics)
        Spacer(modifier = Modifier.height(spacing.medium))
        TextInput(
            modifier = Modifier.focusRequester(focusRequester),
            value = header.value,
            onValueChange = { str -> header.value = str },
            placeholder = stringResource(id = R.string.subject_optional),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            imeAction = ImeAction.Next
        )
        TextInput(
            modifier = Modifier.fillMaxSize(),
            value = body.value,
            onValueChange = { str ->
                body.value = str
            },
            placeholder = stringResource(id = R.string.body_text),
        )
    }
}

@Composable
private fun TopicBar(topics: MutableState<List<Topic>>, myTopics: List<Topic>) {
    val showTopicSelection = remember { mutableStateOf(false) }
    if (showTopicSelection.value) TopicSelectionBottomSheet(
        topics = topics,
        myTopics = myTopics,
        onDismiss = { showTopicSelection.value = false })
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.bigScreenEdge)
    ) {
        if (topics.value.isEmpty()) item {
            AddTopicChip(onOpenTopicSelection = { showTopicSelection.value = true })
        }
    }
}

@Composable
private fun AddTopicChip(onOpenTopicSelection: Fn) {
    SmallAssistChip(
        onClick = onOpenTopicSelection,
        label = { Text(text = stringResource(id = R.string.topics)) },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                imageVector = AddIcon,
                contentDescription = stringResource(id = R.string.topics),
            )
        },
        border = BorderStroke(width = spacing.tiny, brush = FancyBrush)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicSelectionBottomSheet(
    topics: MutableState<List<Topic>>,
    myTopics: List<Topic>,
    onDismiss: Fn
) {
    val unselectedTopics = remember {
        derivedStateOf { myTopics - topics.value.toSet() }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            SelectedTopics(
                topics = topics.value,
                onRemoveTopic = { toRemove -> topics.value -= toRemove })
            UnselectedTopics(
                topics = unselectedTopics.value,
                onSelectTopic = { toAdd -> topics.value += toAdd })
        }
    }
}

@Composable
private fun SelectedTopics(topics: List<Topic>, onRemoveTopic: (Topic) -> Unit) {
    Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Text(
            text = stringResource(id = R.string.selected_),
            style = MaterialTheme.typography.titleLarge
        )
        LazyRow {
            if (topics.isEmpty()) item {
                Text(
                    text = stringResource(id = R.string.no_topics_selected),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(topics) {
                TopicChip(
                    topic = it,
                    trailingImageVector = RemoveCircleIcon,
                    onClick = { onRemoveTopic(it) })
            }
        }
    }
}

@Composable
private fun UnselectedTopics(topics: List<Topic>, onSelectTopic: (Topic) -> Unit) {
    SelectionRow(header = stringResource(id = R.string.my_topics)) {
        if (topics.isEmpty()) {
            Text(
                text = stringResource(id = R.string.you_dont_follow_any_topics),
                style = MaterialTheme.typography.titleMedium
            )
        } else LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(count = 3)

        ) {
            if (topics.isEmpty()) item {
                Text(
                    text = stringResource(id = R.string.you_dont_follow_any_topics),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(topics) {
                TopicChip(
                    topic = it,
                    trailingImageVector = AddIcon,
                    onClick = { onSelectTopic(it) })
            }
        }
    }
}

@Composable
private fun SelectionRow(header: String, content: ComposableContent) {
    Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Text(
            text = header,
            style = MaterialTheme.typography.titleLarge
        )
        content()
    }
}
