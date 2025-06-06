package com.dluvian.voyage.ui.components.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R

@Composable
fun BaseAddDialog(
    header: String,
    focusRequester: FocusRequester,
    main: @Composable () -> Unit,
    onDismiss: () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    nextButton: @Composable () -> Unit = {}
) {
    LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = header) },
        text = main,
        confirmButton = {
            Row {
                nextButton()
                confirmButton()
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}
