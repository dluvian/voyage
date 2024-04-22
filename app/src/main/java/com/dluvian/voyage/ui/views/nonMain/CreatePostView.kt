package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.ui.components.TextInput
import com.dluvian.voyage.ui.components.row.TopicSelectionRow
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
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
        TopicSelectionRow(topics = topics, myTopics = myTopics)
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