package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.LoadFullProfile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SaveProfile
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.normalizeName
import com.dluvian.voyage.core.viewModel.EditProfileViewModel
import com.dluvian.voyage.ui.components.button.ExpandToggleTextButton
import com.dluvian.voyage.ui.components.scaffold.SaveableScaffold
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.spacing
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.MetadataRecord

@Composable
fun EditProfileView(
    vm: EditProfileViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isSaving by vm.isSaving
    val fullProfile by vm.fullProfile

    val name = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.name.orEmpty()))
    }
    val about = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.about.orEmpty()))
    }
    val picture = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.picture.orEmpty()))
    }
    val lud06 = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.lud06.orEmpty()))
    }
    val lud16 = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.lud16.orEmpty()))
    }
    val nip05 = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.nip05.orEmpty()))
    }
    val displayName = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.displayName.orEmpty()))
    }
    val website = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.website.orEmpty()))
    }
    val banner = remember(fullProfile) {
        mutableStateOf(TextFieldValue(fullProfile?.banner.orEmpty()))
    }

    val showSaveButton = remember {
        derivedStateOf {
            name.value.text != fullProfile?.name.orEmpty() ||
                    about.value.text != fullProfile?.about.orEmpty() ||
                    picture.value.text != fullProfile?.picture.orEmpty() ||
                    lud06.value.text != fullProfile?.lud06.orEmpty() ||
                    lud16.value.text != fullProfile?.lud16.orEmpty() ||
                    nip05.value.text != fullProfile?.nip05.orEmpty() ||
                    displayName.value.text != fullProfile?.displayName.orEmpty() ||
                    website.value.text != fullProfile?.website.orEmpty() ||
                    banner.value.text != fullProfile?.banner.orEmpty()
        }
    }

    LaunchedEffect(key1 = Unit) {
        onUpdate(LoadFullProfile)
    }

    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)
    val context = LocalContext.current

    SaveableScaffold(
        showSaveButton = showSaveButton.value,
        isSaving = isSaving,
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
                displayName = displayName.value.text,
                website = website.value.text,
                banner = banner.value.text,
            )
            val metadata = Metadata.fromRecord(r = metadataRecord)
            onUpdate(
                SaveProfile(
                    metadata = metadata,
                    signerLauncher = signerLauncher,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
            )
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
            displayName = displayName,
            website = website,
            banner = banner,
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
    displayName: MutableState<TextFieldValue>,
    website: MutableState<TextFieldValue>,
    banner: MutableState<TextFieldValue>
) {
    val showAdvanced = remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Name(username = name) }
        item { About(about = about) }
        item { ShowAdvancedButton(showAdvanced = showAdvanced) }
        item { DisplayName(showAdvanced.value, displayName = displayName) }
        item { Picture(isVisible = showAdvanced.value, picture = picture) }
        item { Lud16(isVisible = showAdvanced.value, lud16 = lud16) }
        item { Lud06(isVisible = showAdvanced.value, lud06 = lud06) }
        item { Nip05(isVisible = showAdvanced.value, nip05 = nip05) }
        item { Website(isVisible = showAdvanced.value, website = website) }
        item { Banner(isVisible = showAdvanced.value, banner = banner) }
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
        isSingleLine = false
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
private fun DisplayName(isVisible: Boolean, displayName: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.display_name),
        input = displayName,
        placeholder = stringResource(id = R.string.enter_your_display_name),
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
private fun Lud16(isVisible: Boolean, lud16: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
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
private fun Website(isVisible: Boolean, website: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.website),
        input = website,
        placeholder = stringResource(id = R.string.enter_your_website),
        keyboardType = KeyboardType.Uri
    )
}

@Composable
private fun Banner(isVisible: Boolean, banner: MutableState<TextFieldValue>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.banner_url),
        input = banner,
        placeholder = stringResource(id = R.string.enter_your_banner_url),
        isSingleLine = true,
        keyboardType = KeyboardType.Uri
    )
}

@Composable
private fun EditableField(
    isVisible: Boolean,
    header: String,
    input: MutableState<TextFieldValue>,
    placeholder: String,
    isSingleLine: Boolean = true,
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
            isSingleLine = isSingleLine,
            keyboardType = keyboardType
        )
    }
}
