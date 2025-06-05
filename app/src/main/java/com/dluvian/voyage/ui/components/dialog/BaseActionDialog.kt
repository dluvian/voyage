package com.dluvian.voyage.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn

Composable () ->Unit
import com.dluvian.voyage.core.Fn

@Composable
fun BaseActionDialog(
    title: String,
    main: @Composable () -> Unit,
    confirmIsEnabled: Boolean = true,
    confirmText: String = stringResource(id = R.string.confirm),
    cancelText: String = stringResource(id = R.string.cancel),
    icon: ImageVector? = null,
    iconTint: Color? = null,
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
        icon = {
            icon?.let {
                Icon(
                    imageVector = it,
                    tint = iconTint ?: LocalContentColor.current,
                    contentDescription = null
                )
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
    icon: ImageVector? = null,
    iconTint: Color? = null,
    onConfirm: Fn,
    onDismiss: Fn,
) {
    BaseActionDialog(
        title = title,
        main = { Text(text = text) },
        confirmIsEnabled = confirmIsEnabled,
        confirmText = confirmText,
        cancelText = cancelText,
        icon = icon,
        iconTint = iconTint,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
