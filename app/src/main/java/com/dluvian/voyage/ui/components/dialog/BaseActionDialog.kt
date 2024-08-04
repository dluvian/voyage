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
fun BaseActionDialog(
    title: String,
    main: ComposableContent,
    confirmIsEnabled: Boolean = true,
    confirmText: String = stringResource(id = R.string.confirm),
    cancelText: String = stringResource(id = R.string.cancel),
    onConfirm: Fn,
    onDismiss: Fn,
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { main() },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmIsEnabled,
                onClick = {
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
fun BaseActionDialog(
    title: String,
    text: String,
    confirmIsEnabled: Boolean = true,
    confirmText: String = stringResource(id = R.string.confirm),
    cancelText: String = stringResource(id = R.string.cancel),
    onConfirm: Fn,
    onDismiss: Fn,
) {
    BaseActionDialog(
        title = title,
        main = { Text(text = text) },
        confirmIsEnabled = confirmIsEnabled,
        confirmText = confirmText,
        cancelText = cancelText,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
