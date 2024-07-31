package com.dluvian.voyage.ui.components.dialog

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.utils.normalizeMuteWord


@Composable
fun AddMuteWordDialog(
    showNext: Boolean,
    onAdd: (String) -> Unit,
    onDismiss: Fn,
) {
    val input = remember { mutableStateOf(TextFieldValue("")) }
    val showConfirmationButton = remember(input.value) {
        input.value.text.isNotEmpty()
    }
    val focusRequester = remember { FocusRequester() }

    BaseAddDialog(
        header = stringResource(id = R.string.add_word),
        focusRequester = focusRequester,
        main = {
            TextField(
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                value = input.value,
                onValueChange = { input.value = it },
                placeholder = { Text(text = stringResource(id = R.string.add_word)) })
        },
        onDismiss = {
            onDismiss()
            input.value = TextFieldValue("")
        },
        confirmButton = {
            if (showConfirmationButton) {
                TextButton(
                    onClick = {
                        onAdd(input.value.text.normalizeMuteWord())
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.add))
                }
            }
        },
        nextButton = {
            if (showNext && showConfirmationButton) {
                TextButton(
                    onClick = {
                        onAdd(input.value.text.normalizeMuteWord())
                        input.value = TextFieldValue("")
                    }) {
                    Text(text = stringResource(id = R.string.next))
                }
            }
        }
    )
}
