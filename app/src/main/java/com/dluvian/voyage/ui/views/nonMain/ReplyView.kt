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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.SendReply
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.ui.components.bottomSheet.FullPostBottomSheet
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.components.text.InputWithSuggestions
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.ExpandIcon
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.ReplyViewModel

@Composable
fun ReplyView(
    vm: ReplyViewModel,
    searchSuggestions: State<List<TrustProfile>>,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val reply by vm.reply
    val parent by vm.parent
    val suggestions by searchSuggestions

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    ContentCreationScaffold(
        showSendButton = reply.text.isNotBlank(),
        snackbar = snackbar,
        onSend = {
            parent?.let {
                onUpdate(SendReply(parent = it.event, content = reply.text))
            }
        },
        onUpdate = onUpdate,
    ) {
        CreateReplyViewContent(
            parent = parent,
            reply = vm.reply,
            searchSuggestions = suggestions,
            focusRequester = focusRequester,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateReplyViewContent(
    parent: UIEvent?,
    reply: MutableState<TextFieldValue>,
    searchSuggestions: List<TrustProfile>,
    focusRequester: FocusRequester,
    onUpdate: (Cmd) -> Unit,
) {
    InputWithSuggestions(
        body = reply,
        searchSuggestions = searchSuggestions,
        onUpdate = onUpdate
    ) {
        if (parent != null) {
            Parent(parent)
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
            value = reply.value,
            onValueChange = { txt -> reply.value = txt },
            placeholder = stringResource(id = R.string.your_reply),
        )
    }
}

@Composable
private fun Parent(parent: UIEvent) {
    val showFullParent = remember { mutableStateOf(false) }
    if (showFullParent.value) FullPostBottomSheet(
        content = parent.annotatedContent,
        onDismiss = { showFullParent.value = false })
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.bigScreenEdge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = true),
            text = parent.annotatedContent,
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
