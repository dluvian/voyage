package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.utils.copyAndToast

@Composable
fun CopyableText(
    text: String,
    toCopy: String = text,
    toast: String = stringResource(id = R.string.value_copied)
) {
    val context = LocalContext.current
    val clip = LocalClipboard.current
    Text(
        modifier = Modifier.clickable {
            copyAndToast(text = toCopy, toast = toast, context = context, clip = clip)
        },
        text = text
    )
}
