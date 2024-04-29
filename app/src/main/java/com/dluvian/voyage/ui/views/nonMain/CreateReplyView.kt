package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Column
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
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.viewModel.CreateReplyViewModel
import com.dluvian.voyage.ui.components.TextInput
import com.dluvian.voyage.ui.components.bottomSheet.FullPostBottomSheet
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.theme.ExpandIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreateReplyView(
    vm: CreateReplyViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isSendingResponse by vm.isSendingReply
    val response = remember { mutableStateOf("") }
    val parent by vm.parent
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)

    ContentCreationScaffold(
        showSendButton = response.value.isNotBlank(),
        isSendingContent = isSendingResponse,
        snackbar = snackbar,
        onSend = {
            parent?.let {
                onUpdate(
                    SendReply(
                        parent = it,
                        body = response.value,
                        context = context,
                        signerLauncher = signerLauncher,
                        onGoBack = { onUpdate(GoBack) })
                )
            }
        },
        onUpdate = onUpdate
    ) {
        CreateResponseViewContent(
            parent = parent,
            response = response,
            focusRequester = focusRequester
        )
    }
}

@Composable
fun CreateResponseViewContent(
    parent: IParentUI?,
    response: MutableState<String>,
    focusRequester: FocusRequester
) {
    Column {
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
            value = TextFieldValue(response.value),
            onValueChange = { str -> response.value = str.text },
            placeholder = stringResource(id = R.string.your_reply),
        )
    }
}

@Composable
private fun Parent(parent: IParentUI) {
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
