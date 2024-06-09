package com.dluvian.voyage.ui.components.button

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.RemoveIcon

@Composable
fun RemoveIconButton(onRemove: Fn) {
    IconButton(onClick = onRemove) {
        Icon(
            imageVector = RemoveIcon,
            contentDescription = stringResource(id = R.string.save),
            tint = Color.Red
        )
    }
}
