package com.dluvian.voyage.ui.views.nonMain.createPost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.CreatePostViewModel

@Composable
fun CreatePostView(vm: CreatePostViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    CreatePostScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        CreatePostContent()
    }
}

@Composable
fun CreatePostContent() {
    val header = remember { mutableStateOf("") }
    val body = remember { mutableStateOf("") }
    Column {
        CreationField(
            value = header.value,
            onValueChange = { str -> header.value = str },
            placeholder = stringResource(id = R.string.title_optional),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            imeAction = ImeAction.Next
        )
        CreationField(
            modifier = Modifier.fillMaxSize(),
            value = body.value,
            onValueChange = { str -> body.value = str },
            placeholder = stringResource(id = R.string.body_text),
        )
    }
}

@Composable
private fun CreationField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    imeAction: ImeAction = ImeAction.Default
) {
    val transparentTextFieldColor = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        colors = transparentTextFieldColor,
        textStyle = style,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        placeholder = {
            Text(
                text = placeholder,
                style = style,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        })
}
