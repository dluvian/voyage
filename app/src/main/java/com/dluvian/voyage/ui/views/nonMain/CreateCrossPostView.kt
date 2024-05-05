package com.dluvian.voyage.ui.views.nonMain


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendCrossPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UpdateCrossPostTopics
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.viewModel.CreateCrossPostViewModel
import com.dluvian.voyage.ui.components.TopicSelectionChips
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.theme.CrossPostIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CreateCrossPostView(
    vm: CreateCrossPostViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isSending by vm.isSending
    val myTopics by vm.myTopics
    val selectedTopics = remember { mutableStateOf(emptyList<Topic>()) }

    ContentCreationScaffold(
        showSendButton = false,
        isSendingContent = isSending,
        snackbar = snackbar,
        title = stringResource(
            id = R.string.cross_post_to_topics_n_of_m,
            selectedTopics.value.size, MAX_TOPICS
        ),
        onSend = { }, // We don't use top bar for sending
        onUpdate = onUpdate,
    ) {
        CreateCrossPostViewContent(
            myTopics = myTopics,
            selectedTopics = selectedTopics,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreateCrossPostViewContent(
    myTopics: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
    onUpdate: OnUpdate,
) {
    LaunchedEffect(key1 = Unit) {
        onUpdate(UpdateCrossPostTopics)
    }

    Column(modifier = Modifier.padding(horizontal = spacing.screenEdge)) {
        TopicSelectionChips(
            modifier = Modifier.weight(1f, fill = false),
            myTopics = myTopics,
            selectedTopics = selectedTopics
        )
        CrossPostButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.xxl),
            selectedTopics = selectedTopics,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CrossPostButton(
    modifier: Modifier = Modifier,
    selectedTopics: State<List<Topic>>,
    onUpdate: OnUpdate
) {
    val context = LocalContext.current
    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)

    Button(
        modifier = modifier,
        onClick = {
            onUpdate(
                SendCrossPost(
                    topics = selectedTopics.value,
                    context = context,
                    signerLauncher = signerLauncher,
                    onGoBack = { onUpdate(GoBack) })
            )
        }) {
        Icon(
            modifier = Modifier.size(sizing.smallIndicator),
            imageVector = CrossPostIcon,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(spacing.small))
        if (selectedTopics.value.isEmpty()) {
            Text(text = stringResource(id = R.string.cross_post_without_topics))
        } else {
            Text(
                text = stringResource(
                    id = R.string.cross_post_to_n_topics,
                    selectedTopics.value.size
                )
            )
        }
    }
}
