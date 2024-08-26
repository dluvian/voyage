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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendGitIssue
import com.dluvian.voyage.core.model.BugReport
import com.dluvian.voyage.core.model.EnhancementRequest
import com.dluvian.voyage.core.model.LabledGitIssue
import com.dluvian.voyage.core.viewModel.CreateGitIssueViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.selection.NamedRadio
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreateGitIsueView(
    vm: CreateGitIssueViewModel,
    searchSuggestions: State<List<AdvancedProfileView>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val header = remember { mutableStateOf(TextFieldValue()) }
    val body = remember { mutableStateOf(TextFieldValue()) }
    val type: MutableState<LabledGitIssue> = remember { mutableStateOf(BugReport()) }
    val isAnon = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = header.value.text.isNotBlank(),
        isSendingContent = vm.isSendingIssue.value,
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendGitIssue(
                    issue = when (val issue = type.value) {
                        is BugReport -> issue.copy(
                            header = header.value.text,
                            body = header.value.text
                        )

                        is EnhancementRequest -> issue.copy(
                            header = header.value.text,
                            body = header.value.text
                        )
                    },
                    isAnon = isAnon.value,
                    context = context,
                    onGoBack = { onUpdate(GoBack) }
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
            isAnon = isAnon,
            focusRequester = focusRequester,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateGitIssueContent(
    header: MutableState<TextFieldValue>,
    body: MutableState<TextFieldValue>,
    type: MutableState<LabledGitIssue>,
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
        IssueTypeSelection(type = type)
        TextInput(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = header.value,
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
private fun IssueTypeSelection(type: MutableState<LabledGitIssue>) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        item {
            NamedRadio(
                isSelected = type.value is BugReport,
                name = stringResource(id = R.string.bug_report),
                onClick = { type.value = BugReport() })
        }
        item { Spacer(modifier = Modifier.width(spacing.large)) }
        item {
            NamedRadio(
                isSelected = type.value is EnhancementRequest,
                name = stringResource(id = R.string.enhancement_request),
                onClick = { type.value = EnhancementRequest() })
        }
    }
}
