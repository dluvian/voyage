package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.DeleteAllPosts
import com.dluvian.voyage.core.ExportDatabase
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.LoadSeed
import com.dluvian.voyage.core.MAX_RETAIN_ROOT
import com.dluvian.voyage.core.MIN_RETAIN_ROOT
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SendAuth
import com.dluvian.voyage.core.UpdateLocalRelayPort
import com.dluvian.voyage.core.UpdateRootPostThreshold
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.toShortenedNpub
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.data.nostr.LOCAL_WEBSOCKET
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.ui.components.bottomSheet.SeedBottomSheet
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.indicator.SmallCircleProgressIndicator
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.spacing
import kotlinx.coroutines.CoroutineScope
import kotlin.math.abs

@Composable
fun SettingsView(vm: SettingsViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.settings),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        SettingsViewContent(vm = vm, onUpdate = onUpdate)
    }
}

@Composable
private fun SettingsViewContent(vm: SettingsViewModel, onUpdate: OnUpdate) {
    LazyColumn {
        if (vm.isLoadingAccount.value) item { FullLinearProgressIndicator() }
        item {
            AccountSection(
                accountType = vm.accountType.value,
                seed = vm.seed.value,
                onUpdate = onUpdate
            )
        }
        item {
            RelaySection(
                localRelayPort = vm.localRelayPort.value,
                sendAuth = vm.sendAuth.value,
                onUpdate = onUpdate
            )
        }
        item {
            DatabaseSection(vm = vm, onUpdate = onUpdate)
        }
        item {
            AppSection()
        }
    }
}

@Composable
private fun AccountSection(
    accountType: AccountType,
    seed: List<String>,
    onUpdate: OnUpdate
) {
    SettingsSection(header = stringResource(id = R.string.account)) {
        val shortenedNpub = remember(accountType) { accountType.publicKey.toShortenedNpub() }
        ClickableRow(
            header = when (accountType) {
                is ExternalAccount -> stringResource(id = R.string.external_signer)
                is DefaultAccount -> stringResource(id = R.string.default_account)
            },
            text = shortenedNpub,
            leadingIcon = AccountIcon,
            onClick = {
                onUpdate(OpenProfile(nprofile = createNprofile(pubkey = accountType.publicKey)))
            }
        ) {
            AccountRowButton(accountType = accountType, onUpdate = onUpdate)
        }
        if (accountType is DefaultAccount) {
            val showSeed = remember { mutableStateOf(false) }
            ClickableRow(
                header = stringResource(id = R.string.recovery_phrase),
                text = stringResource(id = R.string.click_to_show_recovery_phrase),
                onClick = { showSeed.value = true }
            )
            if (showSeed.value) SeedBottomSheet(
                seed = seed,
                onLoadSeed = { onUpdate(LoadSeed) },
                onDismiss = { showSeed.value = false })
        }
    }
}

@Composable
private fun RelaySection(localRelayPort: Int?, sendAuth: Boolean, onUpdate: OnUpdate) {
    val newPort = remember(localRelayPort) { mutableStateOf(localRelayPort?.toString().orEmpty()) }
    LaunchedEffect(key1 = newPort.value) {
        val parsed = runCatching { newPort.value.toInt() }.getOrNull()
        if (parsed != localRelayPort) onUpdate(UpdateLocalRelayPort(port = parsed))
    }
    val showTextField = remember { mutableStateOf(false) }

    SettingsSection(header = stringResource(id = R.string.relays)) {
        val headerSuffix = if (newPort.value.isNotEmpty()) ": ${newPort.value}" else ""
        ClickableRow(
            header = stringResource(id = R.string.local_relay_port) + headerSuffix,
            text = stringResource(id = R.string.port_number_of_your_local_relay),
            onClick = { showTextField.value = !showTextField.value }
        ) {
            AnimatedVisibility(visible = showTextField.value) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.bigScreenEdge)
                        .padding(bottom = spacing.bigScreenEdge),
                    value = newPort.value,
                    prefix = { Text(text = LOCAL_WEBSOCKET) },
                    onValueChange = { newStr ->
                        val trimmed = newStr.trim()
                        runCatching { Integer.valueOf(trimmed) }
                            .onSuccess { parsed -> newPort.value = abs(parsed).toString() }
                            .onFailure { if (trimmed.isEmpty()) newPort.value = trimmed }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                )
            }
        }

        ClickableRow(
            header = stringResource(id = R.string.authenticate_via_auth),
            text = stringResource(id = R.string.enable_to_authenticate_yourself_to_relays),
            trailingContent = {
                Checkbox(
                    checked = sendAuth,
                    onCheckedChange = { onUpdate(SendAuth(sendAuth = it)) },
                )
            },
            onClick = { onUpdate(SendAuth(sendAuth = !sendAuth)) }
        )
    }
}

@Composable
private fun DatabaseSection(
    vm: SettingsViewModel,
    onUpdate: OnUpdate
) {
    val localRootPostThreshold = remember(vm.rootPostThreshold.intValue) {
        mutableFloatStateOf(vm.rootPostThreshold.intValue.toFloat())
    }
    val showSlider = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    SettingsSection(header = stringResource(id = R.string.database)) {
        ClickableRow(
            header = stringResource(
                id = R.string.keep_at_least_n_root_posts,
                localRootPostThreshold.floatValue.toInt()
            ),
            text = stringResource(
                id = R.string.currently_n_root_posts_in_db,
                vm.currentRootPostCount.collectAsState().value
            ),
            onClick = { showSlider.value = !showSlider.value }
        ) {
            AnimatedVisibility(visible = showSlider.value) {
                Slider(
                    modifier = Modifier.padding(horizontal = spacing.bigScreenEdge),
                    value = localRootPostThreshold.floatValue,
                    onValueChange = { localRootPostThreshold.floatValue = it },
                    onValueChangeFinished = {
                        onUpdate(UpdateRootPostThreshold(threshold = localRootPostThreshold.floatValue))
                    },
                    valueRange = MIN_RETAIN_ROOT..MAX_RETAIN_ROOT
                )
            }
        }

        val isExporting = vm.isExporting.value
        val exportCount = vm.exportCount.intValue
        ClickableRow(
            header = stringResource(id = R.string.export_database),
            text = if (isExporting && exportCount > 0) {
                stringResource(id = R.string.exporting_n_posts, exportCount)
            } else {
                stringResource(id = R.string.export_your_posts_and_bookmarks)
            },
            onClick = { onUpdate(ExportDatabase(uiScope = scope)) },
            trailingContent = {
                if (isExporting) SmallCircleProgressIndicator()
            }
        )

        val isDeleting = vm.isDeleting.value
        val showDeleteDialog = remember { mutableStateOf(false) }
        if (showDeleteDialog.value) DeleteAllPostsDialog(
            scope = scope,
            onDismiss = { showDeleteDialog.value = false },
            onUpdate = onUpdate
        )
        ClickableRow(
            header = stringResource(id = R.string.delete_posts),
            text = stringResource(id = R.string.remove_all_posts_from_database),
            onClick = { showDeleteDialog.value = true },
            trailingContent = {
                if (isDeleting) SmallCircleProgressIndicator()
            }
        )
    }
}

@Composable
private fun AppSection() {
    SettingsSection(header = stringResource(id = R.string.app)) {
        ClickableRow(
            header = stringResource(id = R.string.version),
            text = stringResource(id = R.string.version_nr),
        )
    }
}

@Composable
private fun AccountRowButton(
    accountType: AccountType,
    onUpdate: OnUpdate
) {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        when (accountType) {
            is ExternalAccount -> TextButton(onClick = { onUpdate(UseDefaultAccount) }) {
                Text(text = stringResource(id = R.string.logout))
            }

            is DefaultAccount -> TextButton(onClick = {
                onUpdate(RequestExternalAccount(context = context))
            }) {
                Text(text = stringResource(id = R.string.login_with_external_signer))
            }
        }
    }
}

@Composable
private fun SettingsSection(header: String, content: ComposableContent) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(horizontal = spacing.bigScreenEdge)
                .padding(top = spacing.xl, bottom = spacing.small),
            text = header,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
        Spacer(modifier = Modifier.height(spacing.screenEdge))
    }
}

@Composable
private fun DeleteAllPostsDialog(scope: CoroutineScope, onDismiss: Fn, onUpdate: OnUpdate) {
    AlertDialog(
        title = { Text(text = stringResource(id = R.string.delete_posts)) },
        text = { Text(text = stringResource(id = R.string.are_you_sure_you_want_to_delete_all_posts_from_the_database)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onUpdate(DeleteAllPosts(uiScope = scope))
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}
