package com.dluvian.voyage.ui.components.button

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.theme.SaveIcon

@Composable
fun SaveIconButton(onSave: () -> Unit) {
    IconButton(onClick = onSave) {
        Icon(imageVector = SaveIcon, contentDescription = stringResource(id = R.string.save))
    }
}
