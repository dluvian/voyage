package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.R
import com.dluvian.voyage.model.AddClientTag
import com.dluvian.voyage.model.BunkerSigner
import com.dluvian.voyage.model.ChangeUpvoteContent
import com.dluvian.voyage.model.ClickCreateGitIssue
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.LoadSeed
import com.dluvian.voyage.model.MnemonicSigner
import com.dluvian.voyage.model.NsecSigner
import com.dluvian.voyage.model.OpenProfile
import com.dluvian.voyage.model.SendAuth
import com.dluvian.voyage.model.SignerType
import com.dluvian.voyage.shortenedNpub
import com.dluvian.voyage.ui.components.bottomSheet.SeedBottomSheet
import com.dluvian.voyage.ui.components.dialog.BaseActionDialog
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.components.text.AltSectionHeader
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.SettingsViewModel
import rust.nostr.sdk.Nip19Profile

@Composable
fun SettingsView(vm: SettingsViewModel, snackbar: SnackbarHostState, onUpdate: (Cmd) -> Unit) {
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.settings),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        SettingsViewContent(vm = vm, onUpdate = onUpdate)
    }
}

@Composable
private fun SettingsViewContent(vm: SettingsViewModel, onUpdate: (Cmd) -> Unit) {
    val scope = rememberCoroutineScope()
    LazyColumn {
        if (vm.isLoadingAccount.value) item { FullLinearProgressIndicator() }
        vm.signer.value?.let { signer ->
            item {
                AccountSection(
                    signerType = signer,
                    vm = vm,
                    onUpdate = onUpdate
                )
            }
        }
        item {
            RelaySection(vm = vm, onUpdate = onUpdate)
        }
        item {
            AppSection(vm = vm, onUpdate = onUpdate)
        }
    }
}

@Composable
private fun AccountSection(
    signerType: SignerType,
    vm: SettingsViewModel,
    onUpdate: (Cmd) -> Unit
) {
    SettingsSection(header = stringResource(id = R.string.account)) {
        val shortenedNpub = remember(signerType) { signerType.pubkey.shortenedNpub() }
        ClickableRow(
            header = when (signerType) {
                is BunkerSigner -> stringResource(id = R.string.external_signer)
                is MnemonicSigner, is NsecSigner -> stringResource(id = R.string.key_signer)
            },
            text = shortenedNpub,
            leadingIcon = AccountIcon,
            onClick = {
                onUpdate(OpenProfile(Nip19Profile(signerType.pubkey)))
            }
        ) {
            AccountRowButton(onUpdate = onUpdate)
        }
        when (signerType) {
            is BunkerSigner -> {
                val showBunker = remember { mutableStateOf(false) }
                ClickableRow(
                    header = stringResource(id = R.string.nostr_connect),
                    text = stringResource(id = R.string.click_to_show_nostr_connect_uri),
                    onClick = { showBunker.value = true }
                )
                if (showBunker.value) TODO("bunker Bottom sheet")
            }

            is NsecSigner -> {
                val showNsec = remember { mutableStateOf(false) }
                ClickableRow(
                    header = stringResource(id = R.string.private_key),
                    text = stringResource(id = R.string.click_to_show_nsec),
                    onClick = { showNsec.value = true }
                )
                if (showNsec.value) TODO("Nsec Bottom sheet")
            }

            is MnemonicSigner -> {
                val showSeed = remember { mutableStateOf(false) }
                ClickableRow(
                    header = stringResource(id = R.string.recovery_phrase),
                    text = stringResource(id = R.string.click_to_show_recovery_phrase),
                    onClick = { showSeed.value = true }
                )
                if (showSeed.value) SeedBottomSheet(
                    seed = vm.seed.value,
                    onLoadSeed = { onUpdate(LoadSeed) },
                    onDismiss = { showSeed.value = false })
            }
        }
    }
}

@Composable
private fun RelaySection(vm: SettingsViewModel, onUpdate: (Cmd) -> Unit) {
    SettingsSection(header = stringResource(id = R.string.relays)) {
        ClickableRowCheckbox(
            header = stringResource(id = R.string.authenticate_via_auth),
            text = stringResource(id = R.string.enable_to_authenticate_yourself_to_relays),
            checked = vm.sendAuth.value,
            onClickChange = { onUpdate(SendAuth(sendAuth = it)) })
    }
}

@Composable
private fun AppSection(vm: SettingsViewModel, onUpdate: (Cmd) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    SettingsSection(header = stringResource(id = R.string.app)) {
        val showUpvoteDialog = remember { mutableStateOf(false) }
        if (showUpvoteDialog.value) {
            val newUpvote = remember { mutableStateOf(TextFieldValue(vm.currentUpvote.value)) }
            BaseActionDialog(title = stringResource(id = R.string.upvote_event_content),
                main = {
                    LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester = focusRequester),
                        value = newUpvote.value,
                        onValueChange = { newUpvote.value = it },
                        singleLine = true
                    )
                },
                confirmIsEnabled = newUpvote.value.text.trim() != "-",
                onConfirm = { onUpdate(ChangeUpvoteContent(newContent = newUpvote.value.text)) },
                onDismiss = { showUpvoteDialog.value = false })
        }

        ClickableRowCheckbox(
            header = stringResource(id = R.string.add_client_tag),
            text = stringResource(id = R.string.let_other_clients_know_that_you_are_posting_with_voyage),
            checked = vm.isAddingClientTag.value,
            onClickChange = { onUpdate(AddClientTag(addClientTag = it)) })

        ClickableRow(
            header = stringResource(id = R.string.upvote_event_content) + ": ${vm.currentUpvote.value}",
            text = stringResource(id = R.string.this_affects_how_other_clients_render_your_upvotes),
            onClick = { showUpvoteDialog.value = true })

        ClickableRow(
            header = stringResource(id = R.string.give_us_feedback),
            text = stringResource(id = R.string.write_a_bug_report_or_feature_request),
            onClick = { onUpdate(ClickCreateGitIssue) })

        ClickableRow(
            header = stringResource(id = R.string.version),
            text = stringResource(id = R.string.version_nr),
        )
    }
}

@Composable
private fun AccountRowButton(
    onUpdate: (Cmd) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        val showLoginDialog = remember { mutableStateOf(false) }
        TextButton(onClick = {
            showLoginDialog.value = true
        }) {
            Text(text = stringResource(id = R.string.login_with_different_account))
        }
        // TODO: Show login dialog for inputting nsec, mnemonic or nsecbunker
    }
}

@Composable
private fun SettingsSection(header: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AltSectionHeader(header = header)
        content()
        Spacer(modifier = Modifier.height(spacing.screenEdge))
    }
}

@Composable
private fun ClickableRowCheckbox(
    header: String,
    text: String,
    checked: Boolean,
    onClickChange: (Boolean) -> Unit
) {
    ClickableRow(
        header = header,
        text = text,
        trailingContent = {
            Checkbox(checked = checked, onCheckedChange = onClickChange)
        },
        onClick = { onClickChange(!checked) })
}
