package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import com.dluvian.voyage.model.BugReport
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.EnhancementRequest
import com.dluvian.voyage.model.GitIssueType
import com.dluvian.voyage.model.SendGitIssue
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.selection.NamedRadio
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.GitIssueViewModel

@Composable
fun GitIssueView(
    vm: GitIssueViewModel,
    searchSuggestions: State<List<TrustProfile>>,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val header = remember { mutableStateOf(TextFieldValue()) }
    val body = remember { mutableStateOf(TextFieldValue()) }
    val type: MutableState<GitIssueType> = remember { mutableStateOf(BugReport) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = header.value.text.isNotBlank(),
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendGitIssue(
                    type = type.value,
                    header = header.value.text,
                    content = body.value.text,
                )
            )
        },
        onUpdate = onUpdate,
    ) {
        CreateGitIssueContent(
            header = header,
            body = body,
            type = type,
            searchSuggestions = searchSuggestions.value,
            focusRequester = focusRequester,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateGitIssueContent(
    header: MutableState<TextFieldValue>,
    body: MutableState<TextFieldValue>,
    type: MutableState<GitIssueType>,
    searchSuggestions: List<TrustProfile>,
    focusRequester: FocusRequester,
    onUpdate: (Cmd) -> Unit,
) {
    InputWithSuggestions(
        body = body,
        searchSuggestions = searchSuggestions,
        onUpdate = onUpdate
    ) {
        IssueTypeSelection(type = type)
        TextInput(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = header.value,
            maxLines = MAX_LINES_SUBJECT,
            onValueChange = { txt -> header.value = txt },
            placeholder = stringResource(id = R.string.subject),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
        TextInput(
            modifier = Modifier.fillMaxSize(),
            value = body.value,
            onValueChange = { str -> body.value = str },
            placeholder = stringResource(id = R.string.body_text_optional),
        )
    }
}

@Composable
private fun IssueTypeSelection(type: MutableState<GitIssueType>) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        item {
            NamedRadio(
                isSelected = type.value is BugReport,
                name = stringResource(id = R.string.bug_report),
                onClick = { type.value = BugReport })
        }
        item { Spacer(modifier = Modifier.width(spacing.large)) }
        item {
            NamedRadio(
                isSelected = type.value is EnhancementRequest,
                name = stringResource(id = R.string.enhancement_request),
                onClick = { type.value = EnhancementRequest })
        }
    }
}
