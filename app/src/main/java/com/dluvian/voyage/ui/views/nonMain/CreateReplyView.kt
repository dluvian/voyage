package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.viewModel.CreateReplyViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.bottomSheet.FullPostBottomSheet
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.ExpandIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreateReplyView(
    vm: CreateReplyViewModel,
    searchSuggestions: State<List<AdvancedProfileView>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isSendingResponse by vm.isSendingReply
    val response = remember { mutableStateOf(TextFieldValue()) }
    val isAnon = remember { mutableStateOf(false) }
    val parent by vm.parent
    val suggestions by searchSuggestions
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = response.value.text.isNotBlank(),
        isSendingContent = isSendingResponse,
        snackbar = snackbar,
        onSend = {
            parent?.let {
                onUpdate(
                    SendReply(
                        parent = it,
                        body = response.value.text,
                        isAnon = isAnon.value,
                        context = context,
                        onGoBack = { onUpdate(GoBack) })
                )
            }
        },
        onUpdate = onUpdate,
    ) {
        CreateReplyViewContent(
            parent = parent,
            response = response,
            searchSuggestions = suggestions,
            isAnon = isAnon,
            focusRequester = focusRequester,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateReplyViewContent(
    parent: MainEvent?,
    response: MutableState<TextFieldValue>,
    searchSuggestions: List<AdvancedProfileView>,
    isAnon: MutableState<Boolean>,
    focusRequester: FocusRequester,
    onUpdate: OnUpdate,
) {
    InputWithSuggestions(
        body = response,
        searchSuggestions = searchSuggestions,
        isAnon = isAnon,
        onUpdate = onUpdate
    ) {
        if (parent != null) {
            Parent(parent = parent)
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = spacing.screenEdge)
                    .padding(top = spacing.xxl)
            )
        }

        TextInput(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            value = response.value,
            onValueChange = { txt -> response.value = txt },
            placeholder = stringResource(id = R.string.your_reply),
        )
    }
}

@Composable
private fun Parent(parent: MainEvent) {
    val showFullParent = remember { mutableStateOf(false) }
    if (showFullParent.value) FullPostBottomSheet(
        content = parent.content,
        onDismiss = { showFullParent.value = false })
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.bigScreenEdge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = true),
            text = parent.content,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = { showFullParent.value = true }) {
            Icon(
                imageVector = ExpandIcon,
                contentDescription = stringResource(id = R.string.show_original_post)
            )
        }
    }
}
