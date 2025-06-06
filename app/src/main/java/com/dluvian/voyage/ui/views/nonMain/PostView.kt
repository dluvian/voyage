package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.MAX_LINES_SUBJECT
import com.dluvian.voyage.R
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.SendPost
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.row.TopicSelectionRow
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.PostViewModel

@Composable
fun PostView(
    vm: PostViewModel,
    searchSuggestions: State<List<TrustProfile>>,
    topicSuggestions: State<List<Topic>>,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val topics = remember { mutableStateOf(emptyList<Topic>()) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    val showSendButton = remember(vm.content.value) {
        vm.content.value.text.isNotBlank()
    }

    ContentCreationScaffold(
        showSendButton = showSendButton,
        snackbar = snackbar,
        title = stringResource(R.string.post),
        onSend = {
            onUpdate(
                SendPost(
                    topics = topics.value,
                    subject = vm.subject.value.text,
                    content = vm.content.value.text,
                )
            )
        },
        onUpdate = onUpdate,
    ) {
        CreatePostContent(
            header = vm.subject,
            body = vm.content,
            topicSuggestions = topicSuggestions.value,
            selectedTopics = topics,
            searchSuggestions = searchSuggestions.value,
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
    searchSuggestions: List<TrustProfile>,
    focusRequester: FocusRequester,
    onUpdate: (Cmd) -> Unit,
) {
    InputWithSuggestions(
        body = body,
        searchSuggestions = searchSuggestions,
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
            maxLines = MAX_LINES_SUBJECT,
            placeholder = stringResource(id = R.string.subject_optional),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )

        TextInput(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .focusRequester(focusRequester),
            value = body.value,
            onValueChange = { str -> body.value = str },
            placeholder = stringResource(id = R.string.body_text),
        )
    }
}
