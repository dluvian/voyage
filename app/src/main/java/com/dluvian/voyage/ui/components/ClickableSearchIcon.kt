package com.dluvian.voyage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickSearch
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.theme.SearchIcon

@Composable
fun ClickableSearchIcon(onUpdate: OnUpdate) {
    Icon(
        modifier = Modifier.clickable(onClick = { onUpdate(ClickSearch) }),
        imageVector = SearchIcon,
        contentDescription = stringResource(id = R.string.search)
    )
}
