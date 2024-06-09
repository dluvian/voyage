package com.dluvian.voyage.ui.components.button

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickSearch
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.theme.SearchIcon

@Composable
fun SearchIconButton(onUpdate: OnUpdate) {
    IconButton(onClick = { onUpdate(ClickSearch) }) {
        Icon(imageVector = SearchIcon, contentDescription = stringResource(id = R.string.search))
    }
}
