package com.dluvian.voyage.ui.views.nonMain.editProfile

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
import com.dluvian.voyage.R
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.LoadFullProfile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SaveProfile
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.viewModel.EditProfileViewModel
import com.dluvian.voyage.ui.components.TextInput
import com.dluvian.voyage.ui.components.button.ExpandToggleTextButton
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

    val name = remember(fullProfile) { mutableStateOf(fullProfile?.profileEntity?.name.orEmpty()) }
    val about = remember(fullProfile) { mutableStateOf(fullProfile?.about.orEmpty()) }
    val picture = remember(fullProfile) { mutableStateOf(fullProfile?.picture.orEmpty()) }
    val lud06 = remember(fullProfile) { mutableStateOf(fullProfile?.lud06.orEmpty()) }
    val lud16 = remember(fullProfile) { mutableStateOf(fullProfile?.lud16.orEmpty()) }
    val nip05 = remember(fullProfile) { mutableStateOf(fullProfile?.nip05.orEmpty()) }
    val displayName = remember(fullProfile) { mutableStateOf(fullProfile?.displayName.orEmpty()) }
    val website = remember(fullProfile) { mutableStateOf(fullProfile?.website.orEmpty()) }
    val banner = remember(fullProfile) { mutableStateOf(fullProfile?.banner.orEmpty()) }

    val showSaveButton = remember {
        derivedStateOf {
            name.value != fullProfile?.profileEntity?.name.orEmpty() ||
                    about.value != fullProfile?.about.orEmpty() ||
                    picture.value != fullProfile?.picture.orEmpty() ||
                    lud06.value != fullProfile?.lud06.orEmpty() ||
                    lud16.value != fullProfile?.lud16.orEmpty() ||
                    nip05.value != fullProfile?.nip05.orEmpty() ||
                    displayName.value != fullProfile?.displayName.orEmpty() ||
                    website.value != fullProfile?.website.orEmpty() ||
                    banner.value != fullProfile?.banner.orEmpty()
        }
    }

    LaunchedEffect(key1 = Unit) {
        onUpdate(LoadFullProfile)
    }

    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)
    val context = LocalContext.current

    EditProfileScaffold(
        showSaveButton = showSaveButton.value,
        isSaving = isSaving,
        snackbar = snackbar,
        onSave = {
            val metadataRecord = MetadataRecord(
                name = name.value,
                about = about.value,
                picture = picture.value,
                lud06 = lud06.value,
                lud16 = lud16.value,
                nip05 = nip05.value,
                displayName = displayName.value,
                website = website.value,
                banner = banner.value,
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
    name: MutableState<String>,
    about: MutableState<String>,
    picture: MutableState<String>,
    lud06: MutableState<String>,
    lud16: MutableState<String>,
    nip05: MutableState<String>,
    displayName: MutableState<String>,
    website: MutableState<String>,
    banner: MutableState<String>
) {
    val showAdvanced = remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(contentType = "name") { Name(username = name) }
        item(contentType = "about") { About(about = about) }
        item(contentType = "show_adv_btn") { ShowAdvancedButton(showAdvanced = showAdvanced) }
        item(contentType = "disName") { DisplayName(showAdvanced.value, displayName = displayName) }
        item(contentType = "picture") { Picture(isVisible = showAdvanced.value, picture = picture) }
        item(contentType = "lud16") { Lud16(isVisible = showAdvanced.value, lud16 = lud16) }
        item(contentType = "lud06") { Lud06(isVisible = showAdvanced.value, lud06 = lud06) }
        item(contentType = "nip05") { Nip05(isVisible = showAdvanced.value, nip05 = nip05) }
        item(contentType = "website") { Website(isVisible = showAdvanced.value, website = website) }
        item(contentType = "banner") { Banner(isVisible = showAdvanced.value, banner = banner) }
    }
}

@Composable
private fun Name(username: MutableState<String>) {
    EditableField(
        isVisible = true,
        header = stringResource(id = R.string.name),
        input = username,
        placeholder = stringResource(id = R.string.enter_your_name),
    )
}

@Composable
private fun About(about: MutableState<String>) {
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
private fun DisplayName(isVisible: Boolean, displayName: MutableState<String>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.display_name),
        input = displayName,
        placeholder = stringResource(id = R.string.enter_your_display_name),
    )
}

@Composable
private fun Picture(isVisible: Boolean, picture: MutableState<String>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.profile_picture_url),
        input = picture,
        placeholder = stringResource(id = R.string.enter_your_profile_picture_url),
        keyboardType = KeyboardType.Uri
    )
}

@Composable
private fun Lud06(isVisible: Boolean, lud06: MutableState<String>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.lightning_pay_request_lud06),
        input = lud06,
        placeholder = stringResource(id = R.string.enter_your_lud06),
    )
}

@Composable
private fun Lud16(isVisible: Boolean, lud16: MutableState<String>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.lightning_address_lud16),
        input = lud16,
        placeholder = stringResource(id = R.string.enter_your_lightning_address),
        keyboardType = KeyboardType.Email
    )
}

@Composable
private fun Nip05(isVisible: Boolean, nip05: MutableState<String>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.nip05),
        input = nip05,
        placeholder = stringResource(id = R.string.enter_your_nip05),
        keyboardType = KeyboardType.Email
    )
}

@Composable
private fun Website(isVisible: Boolean, website: MutableState<String>) {
    EditableField(
        isVisible = isVisible,
        header = stringResource(id = R.string.website),
        input = website,
        placeholder = stringResource(id = R.string.enter_your_website),
        keyboardType = KeyboardType.Uri
    )
}

@Composable
private fun Banner(isVisible: Boolean, banner: MutableState<String>) {
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
    input: MutableState<String>,
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
            onValueChange = { newStr: String -> input.value = newStr },
            placeholder = placeholder,
            isSingleLine = isSingleLine,
            keyboardType = keyboardType
        )
    }
}
