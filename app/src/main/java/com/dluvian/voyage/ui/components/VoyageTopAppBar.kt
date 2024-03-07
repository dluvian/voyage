package com.dluvian.voyage.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.BackIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageTopAppBar(title: String? = null, showGoBack: Boolean = false, onUpdate: OnUpdate = {}) {
    TopAppBar(
        title = { title?.let { Text(text = title) } },
        navigationIcon = {
            if (showGoBack) IconButton(onClick = { onUpdate(GoBack) }) {
                Icon(
                    imageVector = BackIcon,
                    contentDescription = stringResource(id = R.string.go_back)
                )
            }
        }
    )
}
