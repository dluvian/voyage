package com.dluvian.voyage.ui.views.nonMain.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SearchText
import com.dluvian.voyage.core.UpdateSearchText
import com.dluvian.voyage.ui.components.button.GoBackIconButton
import com.dluvian.voyage.ui.theme.RoundedChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(focusRequester: FocusRequester, onUpdate: OnUpdate) {
    val text = remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    TopAppBar(
        title = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedChip)
                    .focusRequester(focusRequester),
                value = text.value,
                onValueChange = { newText ->
                    onUpdate(UpdateSearchText(text = newText))
                    text.value = newText
                },
                placeholder = { Text(text = stringResource(id = R.string.search_)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Search,
                    autoCorrect = false,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onUpdate(
                            SearchText(text = text.value, context = context, onUpdate = onUpdate)
                        )
                    },
                ),
            )
        },
        navigationIcon = {
            GoBackIconButton(onUpdate = onUpdate)
        },
    )
}
