package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.ui.components.TextInput

@Composable
fun CreatePostView(vm: CreatePostViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val header = remember { mutableStateOf("") }
    val body = remember { mutableStateOf("") }
    val isSendingPost by vm.isSendingPost
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = body.value.isNotBlank(),
        isSendingContent = isSendingPost,
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendPost(
                    header = header.value,
                    body = body.value,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
            )
        },
        onUpdate = onUpdate
    ) {
        CreatePostContent(header = header, body = body, focusRequester = focusRequester)
    }
}

@Composable
private fun CreatePostContent(
    header: MutableState<String>,
    body: MutableState<String>,
    focusRequester: FocusRequester
) {
    Column {
        TextInput(
            modifier = Modifier.focusRequester(focusRequester),
            value = header.value,
            onValueChange = { str -> header.value = str },
            placeholder = stringResource(id = R.string.title_optional),
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
