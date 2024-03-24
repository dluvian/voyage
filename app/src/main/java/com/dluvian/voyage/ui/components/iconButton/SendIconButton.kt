package com.dluvian.voyage.ui.components.iconButton

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickSearch
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.theme.SearchIcon
import com.dluvian.voyage.ui.theme.SendIcon


@Composable
fun SendIconButton(onSend: Fn) {
    IconButton(onClick = onSend) {
        Icon(imageVector = SendIcon, contentDescription = stringResource(id = R.string.send))
    }
}
