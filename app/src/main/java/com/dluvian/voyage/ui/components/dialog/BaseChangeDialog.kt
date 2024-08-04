package com.dluvian.voyage.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn

@Composable
fun BaseChangeDialog(
    title: String,
    main: ComposableContent,
    confirmText: String = stringResource(id = R.string.change),
    cancelText: String = stringResource(id = R.string.cancel),
    onConfirm: Fn,
    onDismiss: Fn,
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { main() },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = cancelText) }
        },
    )
}

@Composable
fun BaseChangeDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(id = R.string.change),
    cancelText: String = stringResource(id = R.string.cancel),
    onConfirm: Fn,
    onDismiss: Fn,
) {
    BaseChangeDialog(
        title = title,
        main = { Text(text = text) },
        confirmText = confirmText,
        cancelText = cancelText,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
