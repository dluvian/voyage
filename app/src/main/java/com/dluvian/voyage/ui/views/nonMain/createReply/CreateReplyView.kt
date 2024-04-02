package com.dluvian.voyage.ui.views.nonMain.createReply

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendReply
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.viewModel.CreateReplyViewModel
import com.dluvian.voyage.ui.components.TextInput
import com.dluvian.voyage.ui.theme.ExpandIcon
import com.dluvian.voyage.ui.theme.light
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.nonMain.createPost.ContentCreationScaffold

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
                        onGoBack = { onUpdate(GoBack) })
                )
            }
        },
        onUpdate = onUpdate
    ) {
        CreateResponseViewContent(parent = parent, response = response)
    }
}

@Composable
fun CreateResponseViewContent(parent: IParentUI?, response: MutableState<String>) {
    Column {
        if (parent != null) Parent(parent = parent)

        TextInput(
            modifier = Modifier.fillMaxSize(),
            value = response.value,
            onValueChange = { str ->
                response.value = str
            },
            placeholder = stringResource(id = R.string.your_reply),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Parent(parent: IParentUI) {
    val showFullParent = remember { mutableStateOf(false) }
    FullPostBottomSheet(
        isVisible = showFullParent.value,
        content = parent.content,
        onDismiss = { showFullParent.value = false })
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.bigScreenEdge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPostBottomSheet(isVisible: Boolean, content: String, onDismiss: Fn) {
    if (isVisible) ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.bigScreenEdge)
        ) {
            Text(
                text = stringResource(id = R.string.original_post),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.light()
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.xl)
            )
            Text(text = content)
        }
    }
}
