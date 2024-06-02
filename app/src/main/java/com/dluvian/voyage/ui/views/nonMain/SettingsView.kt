package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.LoadSeed
import com.dluvian.voyage.core.MAX_RETAIN_ROOT
import com.dluvian.voyage.core.MIN_RETAIN_ROOT
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SendAuth
import com.dluvian.voyage.core.UpdateRootPostThreshold
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.toShortenedNpub
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.ui.components.bottomSheet.SeedBottomSheet
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.spacing

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
    val accountType by vm.accountType
    val seed by vm.seed
    val isLoadingAccount by vm.isLoadingAccount
    val rootPostThreshold by vm.rootPostThreshold
    val sendAuth by vm.sendAuth
    val currentRootPostCount by vm.currentRootPostCount.collectAsState()

    LazyColumn {
        if (isLoadingAccount) item { FullLinearProgressIndicator() }
        item {
            AccountSection(
                accountType = accountType,
                seed = seed,
                onUpdate = onUpdate
            )
        }
        item { RelaySection(sendAuth = sendAuth, onUpdate = onUpdate) }
        item {
            DatabaseSection(
                rootPostThreshold = rootPostThreshold,
                currentRootPostCount = currentRootPostCount,
                onUpdate = onUpdate
            )
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
            imageVector = AccountIcon,
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
private fun RelaySection(sendAuth: Boolean, onUpdate: OnUpdate) {
    SettingsSection(header = stringResource(id = R.string.relays)) {
        ClickableRow(
            header = stringResource(id = R.string.authenticate_via_auth),
            text = stringResource(id = R.string.enable_to_authenticate_yourself_to_relays),
            onClick = { onUpdate(SendAuth(sendAuth = !sendAuth)) },
            trailingContent = {
                Checkbox(
                    checked = sendAuth,
                    onCheckedChange = { onUpdate(SendAuth(sendAuth = it)) },
                )
            }
        )
    }
}

@Composable
private fun DatabaseSection(
    rootPostThreshold: Int,
    currentRootPostCount: Int,
    onUpdate: OnUpdate
) {
    val localRootPostThreshold = remember(rootPostThreshold) {
        mutableFloatStateOf(rootPostThreshold.toFloat())
    }
    SettingsSection(header = stringResource(id = R.string.database)) {
        ClickableRow(
            header = stringResource(
                id = R.string.keep_at_least_n_root_posts,
                localRootPostThreshold.floatValue.toInt()
            ),
            text = stringResource(id = R.string.currently_n_root_posts_in_db, currentRootPostCount),
        ) {
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
}

@Composable
private fun AppSection() {
    SettingsSection(header = stringResource(id = R.string.app)) {
        ClickableRow(
            header = stringResource(id = R.string.version),
            text = stringResource(id = R.string.version_nr)
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
