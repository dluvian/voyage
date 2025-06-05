package com.dluvian.voyage.ui.components.button

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.GoBack
import com.dluvian.voyage.ui.theme.BackIcon

@Composable
fun GoBackIconButton(onUpdate: (Cmd) -> Unit) {
    IconButton(onClick = { onUpdate(GoBack) }) {
        Icon(
            imageVector = BackIcon,
            contentDescription = stringResource(id = R.string.go_back)
        )
    }
}
