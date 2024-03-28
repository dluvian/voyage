package com.dluvian.voyage.ui.views.nonMain.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.model.SimpleNip19Profile
import com.dluvian.voyage.core.shortenBech32
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.ui.components.ClickableRow
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun SettingsView(vm: SettingsViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    SettingsScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        SettingsViewContent(vm = vm, onUpdate = onUpdate)
    }
}

@Composable
private fun SettingsViewContent(vm: SettingsViewModel, onUpdate: OnUpdate) {
    val accountType by vm.accountType
    val isLoadingAccount by vm.isLoadingAccount
    LazyColumn {
        if (isLoadingAccount) item { LinearProgressIndicator() }
        item {
            SettingsSection(header = stringResource(id = R.string.account)) {
                AccountRow(accountType = accountType, onUpdate = onUpdate)
            }
        }
        item {
            SettingsSection(header = stringResource(id = R.string.app)) {
                ClickableRow(
                    header = stringResource(id = R.string.version),
                    text = stringResource(id = R.string.version_nr)
                )
            }
        }
    }

}

@Composable
private fun AccountRow(accountType: AccountType, onUpdate: OnUpdate) {
    val shortenedNpub = remember(accountType) { accountType.publicKey.shortenBech32() }
    ClickableRow(
        header = when (accountType) {
            is ExternalAccount -> stringResource(id = R.string.external_signer)
            is DefaultAccount -> stringResource(id = R.string.default_account)
        },
        text = shortenedNpub,
        imageVector = AccountIcon,
        onClick = { onUpdate(OpenProfile(SimpleNip19Profile(pubkey = accountType.publicKey.toHex()))) }
    ) {
        AccountRowButton(accountType = accountType, onUpdate = onUpdate)
    }
}

@Composable
private fun AccountRowButton(accountType: AccountType, onUpdate: OnUpdate) {
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
            modifier = Modifier.padding(
                horizontal = spacing.bigScreenEdge,
                vertical = spacing.large
            ),
            text = header,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
        Spacer(modifier = Modifier.height(spacing.screenEdge))
    }
}
