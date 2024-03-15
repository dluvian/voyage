package com.dluvian.voyage.ui.components.bar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.ui.components.GoBackIconButton
import com.dluvian.voyage.ui.theme.RoundedChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(onUpdate: OnUpdate) {
    val text = remember {
        mutableStateOf("")
    }
    TopAppBar(
        title = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedChip),
                value = text.value,
                onValueChange = { newText ->
                    onUpdate(UpdateSearchText(text = newText))
                    text.value = newText
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
            )
        },
        navigationIcon = {
            GoBackIconButton(onUpdate = onUpdate)
        },
    )
}
