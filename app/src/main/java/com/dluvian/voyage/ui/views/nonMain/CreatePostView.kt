package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickSuggestion
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SearchSuggestion
import com.dluvian.voyage.core.SendPost
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.TextInput
import com.dluvian.voyage.ui.components.row.ClickableProfileRow
import com.dluvian.voyage.ui.components.row.TopicSelectionRow
import com.dluvian.voyage.ui.components.scaffold.ContentCreationScaffold
import com.dluvian.voyage.ui.theme.spacing
import rust.nostr.protocol.PublicKey

@Composable
fun CreatePostView(
    vm: CreatePostViewModel,
    searchSuggestions: State<List<AdvancedProfileView>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val header = remember { mutableStateOf("") }
    val body = remember { mutableStateOf(TextFieldValue()) }
    val topics = remember { mutableStateOf(emptyList<Topic>()) }
    val myTopics by vm.myTopics
    val isSendingPost by vm.isSendingPost
    val suggestions by searchSuggestions
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
        vm.updateMyTopics()
    }

    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)

    ContentCreationScaffold(
        showSendButton = body.value.text.isNotBlank(),
        isSendingContent = isSendingPost,
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendPost(
                    header = header.value,
                    body = body.value.text,
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
            searchSuggestions = suggestions,
            focusRequester = focusRequester,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun CreatePostContent(
    header: MutableState<String>,
    body: MutableState<TextFieldValue>,
    topics: MutableState<List<Topic>>,
    myTopics: List<Topic>,
    searchSuggestions: List<AdvancedProfileView>,
    focusRequester: FocusRequester,
    onUpdate: OnUpdate,
) {
    val showSuggestions = remember { mutableStateOf(false) }
    remember(body.value) {
        val current = body.value
        val stringUntilCursor = current.text.take(current.selection.end)
        val mentionedName = stringUntilCursor.takeLastWhile { it != '@' }
        if (mentionedName.any { it.isWhitespace() }) {
            showSuggestions.value = false
            return@remember false
        }
        showSuggestions.value = stringUntilCursor.contains("@")
        if (showSuggestions.value) onUpdate(SearchSuggestion(name = mentionedName))
        true
    }

    Column(modifier = Modifier.fillMaxSize(), Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(0.6f, fill = false)) {
        TopicSelectionRow(topics = topics, myTopics = myTopics)
            Spacer(modifier = Modifier.height(spacing.medium))
            TextInput(
                modifier = Modifier.focusRequester(focusRequester),
                value = TextFieldValue(header.value),
                onValueChange = { str -> header.value = str.text },
                placeholder = stringResource(id = R.string.subject_optional),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                imeAction = ImeAction.Next
            )
            TextInput(
                modifier = Modifier.fillMaxSize(),
                value = body.value,
                onValueChange = { str -> body.value = str },
                placeholder = stringResource(id = R.string.body_text),
            )
        }
        if (showSuggestions.value && searchSuggestions.isNotEmpty()) {
            SearchSuggestions(
                modifier = Modifier.weight(0.4f),
                suggestions = searchSuggestions,
                onReplaceSuggestion = { profile ->
                    body.value = body.value.replaceWithSuggestion(pubkey = profile.pubkey)
                    onUpdate(ClickSuggestion)
                }
            )
        }
    }
}


@Composable
private fun SearchSuggestions(
    suggestions: List<AdvancedProfileView>,
    onReplaceSuggestion: (AdvancedProfileView) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        items(suggestions) { profile ->
            Row(modifier = Modifier.fillMaxWidth()) {
                ClickableProfileRow(
                    profile = profile,
                    onClick = { onReplaceSuggestion(profile) })
            }
        }
    }
}

private fun TextFieldValue.replaceWithSuggestion(pubkey: String): TextFieldValue {
    val stringUntilCursor = this.text.take(this.selection.end)
    val stringAfterCursor = this.text.drop(this.selection.end)
    val mentionedName = stringUntilCursor.takeLastWhile { it != '@' }
    if (mentionedName.any { it.isWhitespace() }) return this
    if (!stringUntilCursor.contains("@")) return this

    var newCursorPos: Int
    val text = buildString {
        append(stringUntilCursor.removeSuffix(mentionedName).removeSuffix("@"))
        append("nostr:")
        append(PublicKey.fromHex(pubkey).toBech32())
        append(" ")
        newCursorPos = this.length
        append(stringAfterCursor)
    }

    return this.copy(
        text = text,
        selection = TextRange(newCursorPos),
        composition = null
    )
}
