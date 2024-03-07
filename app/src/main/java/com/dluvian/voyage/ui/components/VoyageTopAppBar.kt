package com.dluvian.voyage.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageTopAppBar(title: String?) {
    TopAppBar(
        title = { title?.let { Text(text = title) } }
    )
}
