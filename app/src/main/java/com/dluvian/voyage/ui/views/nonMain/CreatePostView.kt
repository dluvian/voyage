package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdatePostTopics
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
    val header = remember { mutableStateOf(TextFieldValue()) }
    val body = remember { mutableStateOf(TextFieldValue()) }
    val isAnon = remember { mutableStateOf(false) }
    val topics = remember { mutableStateOf(emptyList<Topic>()) }
    val isSendingPost by vm.isSendingPost
    val suggestions by searchSuggestions
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
        onUpdate(UpdatePostTopics)
    }

    ContentCreationScaffold(
        showSendButton = body.value.text.isNotBlank(),
        isSendingContent = isSendingPost,
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendPost(
                    header = header.value.text,
                    body = body.value.text,
                    topics = topics.value,
                    isAnon = isAnon.value,
                    context = context,
                ) { onUpdate(GoBack) }
            )
        },
        onUpdate = onUpdate,
    ) {
        CreatePostContent(
            header = header,
            body = body,
            topicSuggestions = topicSuggestions.value,
            selectedTopics = topics,
            searchSuggestions = suggestions,
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
