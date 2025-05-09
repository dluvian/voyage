package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.row.TopicSelectionRow
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreatePostView(
    vm: CreatePostViewModel,
    searchSuggestions: State<List<AdvancedProfileView>>,
    topicSuggestions: State<List<Topic>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val body = remember { mutableStateOf(TextFieldValue()) }
    val topics = remember { mutableStateOf(emptyList<Topic>()) }
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = remember(body.value) {
            body.value.text.isNotBlank()
        },
        isSendingContent = vm.isSending.value,
        snackbar = snackbar,
        title = stringResource(R.string.post),
        onSend = {
                SendPost(
                    body = body.value.text,
                    topics = topics.value,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
        },
        onUpdate = onUpdate,
    ) {
        CreatePostContent(
            body = body,
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
    body: MutableState<TextFieldValue>,
    topicSuggestions: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    searchSuggestions: List<AdvancedProfileView>,
    focusRequester: FocusRequester,
    onUpdate: OnUpdate,
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
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .focusRequester(focusRequester),
            value = body.value,
            onValueChange = { str -> body.value = str },
            maxLines = Int.MAX_VALUE,
            placeholder = stringResource(id = R.string.body_text),
        )
    }
}
