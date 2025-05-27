package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.ClickProfileSuggestion
import com.dluvian.voyage.R
import com.dluvian.voyage.SearchProfileSuggestion
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.nostr.NOSTR_URI
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.row.ClickableProfileRow
import com.dluvian.voyage.ui.components.selection.NamedCheckbox
import rust.nostr.sdk.PublicKey

@Composable
fun InputWithSuggestions(
    body: MutableState<TextFieldValue>,
    searchSuggestions: List<AdvancedProfileView>,
    isAnon: MutableState<Boolean>,
    onUpdate: OnUpdate,
    input: ComposableContent
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
        if (showSuggestions.value) onUpdate(SearchProfileSuggestion(name = mentionedName))
        true
    }

    Column(modifier = Modifier.fillMaxSize(), Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(0.6f, fill = false)) {
            input()
        }
        if (showSuggestions.value && searchSuggestions.isNotEmpty()) {
            SearchSuggestions(
                modifier = Modifier.weight(0.4f),
                suggestions = searchSuggestions,
                onReplaceSuggestion = { profile ->
                    body.value = body.value.replaceWithSuggestion(pubkey = profile.pubkey)
                    onUpdate(ClickProfileSuggestion(pubkey = profile.pubkey))
                }
            )
        } else {
            NamedCheckbox(
                isChecked = isAnon.value,
                name = stringResource(id = R.string.create_anonymously),
                onClick = { isAnon.value = !isAnon.value })
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
        append(NOSTR_URI)
        append(PublicKey.parse(pubkey).toBech32())
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
