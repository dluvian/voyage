package com.dluvian.voyage.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.SearchProfileSuggestion
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.row.ClickableProfileRow
import com.dluvian.voyage.ui.theme.sizing

@Composable
fun AddProfileDialog(
    profileSuggestions: List<AdvancedProfileView>,
    onAdd: (AdvancedProfileView) -> Unit,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    val input = remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }

    BaseAddDialog(
        header = stringResource(id = R.string.add_profile),
        focusRequester = focusRequester,
        main = {
            Input(
                input = input,
                profileSuggestions = profileSuggestions,
                focusRequester = focusRequester,
                onAdd = onAdd,
                onUpdate = onUpdate
            )
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun Input(
    input: MutableState<TextFieldValue>,
    profileSuggestions: List<AdvancedProfileView>,
    focusRequester: FocusRequester,
    onAdd: (AdvancedProfileView) -> Unit,
    onUpdate: OnUpdate
) {
    Column {
        TextField(
            modifier = Modifier.focusRequester(focusRequester = focusRequester),
            value = input.value,
            onValueChange = {
                input.value = it
                onUpdate(SearchProfileSuggestion(name = it.text))
            },
            placeholder = { Text(text = stringResource(id = R.string.search_)) })
        if (input.value.text.isNotEmpty()) {
            ProfileSuggestions(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = sizing.dialogLazyListHeight),
                suggestions = profileSuggestions,
                onClickSuggestion = onAdd
            )
        }
    }
}

@Composable
private fun ProfileSuggestions(
    suggestions: List<AdvancedProfileView>,
    onClickSuggestion: (AdvancedProfileView) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(suggestions) { profile ->
                ClickableProfileRow(
                    profile = profile,
                    onClick = { onClickSuggestion(profile) })
        }
    }
}
