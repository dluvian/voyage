package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.MAX_LINES_SUBJECT
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.PublishProfile
import com.dluvian.voyage.normalizeName
import com.dluvian.voyage.ui.components.button.ExpandToggleTextButton
import com.dluvian.voyage.ui.components.scaffold.SaveableScaffold
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.EditProfileViewModel
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.MetadataRecord

@Composable
fun EditProfileView(
    vm: EditProfileViewModel,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val profile by vm.profile

    val name = remember(profile) {
        mutableStateOf(TextFieldValue(profile.getName().orEmpty()))
    }
    val about = remember(profile) {
        mutableStateOf(TextFieldValue(profile.getAbout().orEmpty()))
    }
    val picture = remember(profile) {
        mutableStateOf(TextFieldValue(profile.getPicture().orEmpty()))
    }
    val lud06 = remember(profile) {
        mutableStateOf(TextFieldValue(profile.getLud06().orEmpty()))
    }
    val lud16 = remember(profile) {
        mutableStateOf(TextFieldValue(profile.getLud16().orEmpty()))
    }
    val nip05 = remember(profile) {
        mutableStateOf(TextFieldValue(profile?.getNip05().orEmpty()))
    }

    val showSaveButton = remember {
        derivedStateOf {
            name.value.text != profile.getName().orEmpty() ||
                    about.value.text != profile.getAbout().orEmpty() ||
                    picture.value.text != profile.getPicture().orEmpty() ||
                    lud06.value.text != profile.getLud06().orEmpty() ||
                    lud16.value.text != profile.getLud16().orEmpty() ||
                    nip05.value.text != profile.getNip05().orEmpty()
        }
    }

    SaveableScaffold(
        showSaveButton = showSaveButton.value,
        snackbar = snackbar,
        title = stringResource(id = R.string.edit),
        onSave = {
            val metadataRecord = MetadataRecord(
                name = normalizeName(name.value.text),
                about = about.value.text,
                picture = picture.value.text,
                lud06 = lud06.value.text,
                lud16 = lud16.value.text,
                nip05 = nip05.value.text,
            )
            val metadata = Metadata.fromRecord(r = metadataRecord)
            onUpdate(PublishProfile(metadata))
        },
        onUpdate = onUpdate
    ) {
        EditProfileViewContent(
            name = name,
            about = about,
            picture = picture,
            lud06 = lud06,
            lud16 = lud16,
            nip05 = nip05,
        )
    }
}

@Composable
private fun EditProfileViewContent(
    name: MutableState<TextFieldValue>,
    about: MutableState<TextFieldValue>,
    picture: MutableState<TextFieldValue>,
    lud06: MutableState<TextFieldValue>,
    lud16: MutableState<TextFieldValue>,
    nip05: MutableState<TextFieldValue>,
) {
    val showAdvanced = remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Name(username = name) }
        item { About(about = about) }
        item { Lud16(lud16 = lud16) }
        item { ShowAdvancedButton(showAdvanced = showAdvanced) }
        item { Picture(isVisible = showAdvanced.value, picture = picture) }
        item { Lud06(isVisible = showAdvanced.value, lud06 = lud06) }
        item { Nip05(isVisible = showAdvanced.value, nip05 = nip05) }
    }
}

@Composable
private fun Name(username: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = true,
        header = stringResource(id = R.string.name),
        input = username,
        placeholder = stringResource(id = R.string.enter_your_name),
    )
}

@Composable
private fun About(about: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = true,
        header = stringResource(id = R.string.about),
        input = about,
        placeholder = stringResource(id = R.string.describe_yourself),
        maxLines = MAX_LINES_SUBJECT
    )
}

@Composable
private fun ShowAdvancedButton(showAdvanced: MutableState<Boolean>) {
    ExpandToggleTextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.xl),
        text = stringResource(id = R.string.show_advanced),
        isExpanded = showAdvanced.value,
        onToggle = { showAdvanced.value = !showAdvanced.value }
    )
}

@Composable
private fun Picture(isVisible: Boolean, picture: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.profile_picture_url),
        input = picture,
        placeholder = stringResource(id = R.string.enter_your_profile_picture_url),
        keyboardType = KeyboardType.Uri
    )
}

@Composable
private fun Lud06(isVisible: Boolean, lud06: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.lightning_pay_request_lud06),
        input = lud06,
        placeholder = stringResource(id = R.string.enter_your_lud06),
    )
}

@Composable
private fun Lud16(lud16: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = true,
        header = stringResource(id = R.string.lightning_address_lud16),
        input = lud16,
        placeholder = stringResource(id = R.string.enter_your_lightning_address),
        keyboardType = KeyboardType.Email
    )
}

@Composable
private fun Nip05(isVisible: Boolean, nip05: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.nip05),
        input = nip05,
        placeholder = stringResource(id = R.string.enter_your_nip05),
        keyboardType = KeyboardType.Email
    )
}

@Composable
private fun EditableField(
    isVisible: Boolean,
    header: String,
    input: MutableState<TextFieldValue>,
    placeholder: String,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    AnimatedVisibility(visible = isVisible) {
        Text(
            modifier = Modifier.padding(start = spacing.bigScreenEdge),
            text = header,
            fontWeight = FontWeight.Bold
        )
        TextInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.xxl, top = spacing.xl),
            value = input.value,
            onValueChange = { txt -> input.value = txt },
            placeholder = placeholder,
            maxLines = maxLines,
            keyboardType = keyboardType
        )
    }
}
