package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.MAX_SUBJECT_LINES
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendPoll
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.row.TopicSelectionRow
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.PollIcon
import com.dluvian.voyage.ui.theme.TextIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreatePostView(
    vm: CreatePostViewModel,
    searchSuggestions: State<List<AdvancedProfileView>>,
    topicSuggestions: State<List<Topic>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isPoll = remember { mutableStateOf(false) }
    val options = remember { mutableStateOf(emptyList<TextFieldValue>()) }
    val header = remember { mutableStateOf(TextFieldValue()) }
    val body = remember { mutableStateOf(TextFieldValue()) }
    val isAnon = remember { mutableStateOf(false) }
    val topics = remember { mutableStateOf(emptyList<Topic>()) }
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = body.value.text.isNotBlank(),
        isSendingContent = vm.isSending.value,
        snackbar = snackbar,
        typeIcon = {
            if (!vm.isSending.value) IconButton(onClick = { isPoll.value = !isPoll.value }) {
                Icon(
                    imageVector = if (isPoll.value) PollIcon else TextIcon,
                    contentDescription = if (isPoll.value) stringResource(id = R.string.create_a_text_note)
                    else stringResource(id = R.string.create_a_poll)
                )
            }
        },
        onSend = {
            if (isPoll.value) onUpdate(
                SendPoll(
                    question = body.value.text,
                    options = options.value.map { it.text },
                    topics = topics.value,
                    isAnon = isAnon.value,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
            )
            else onUpdate(
                SendPost(
                    header = header.value.text,
                    body = body.value.text,
                    topics = topics.value,
                    isAnon = isAnon.value,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
            )
        },
        onUpdate = onUpdate,
    ) {
        CreatePostContent(
            header = header,
            body = body,
            topicSuggestions = topicSuggestions.value,
            selectedTopics = topics,
            searchSuggestions = searchSuggestions.value,
            isAnon = isAnon,
            focusRequester = focusRequester,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreatePostContent(
    header: MutableState<TextFieldValue>,
    body: MutableState<TextFieldValue>,
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    searchSuggestions: List<AdvancedProfileView>,
    isAnon: MutableState<Boolean>,
    focusRequester: FocusRequester,
    onUpdate: OnUpdate,
) {
    InputWithSuggestions(
        body = body,
        searchSuggestions = searchSuggestions,
        isAnon = isAnon,
        onUpdate = onUpdate
    ) {
        TopicSelectionRow(
            topicSuggestions = topicSuggestions,
            selectedTopics = selectedTopics,
            onUpdate = onUpdate
        )
        Spacer(modifier = Modifier.height(spacing.medium))
        TextInput(
            value = header.value,
            onValueChange = { txt -> header.value = txt },
            maxLines = MAX_SUBJECT_LINES,
            placeholder = stringResource(id = R.string.subject_optional),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
        TextInput(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            value = body.value,
            onValueChange = { str -> body.value = str },
            placeholder = stringResource(id = R.string.body_text),
        )
    }
}
