package com.dluvian.voyage.ui.views.nonMain.subViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.shortenedBech32
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun SettingsView(vm: SettingsViewModel, onUpdate: OnUpdate) {
    val accountType by vm.accountType
    LazyColumn {
        item {
            SettingsSection(header = stringResource(id = R.string.account)) {
                AccountRow(accountType = accountType, onUpdate = onUpdate)
            }
        }
        item {
            SettingsSection(header = stringResource(id = R.string.app)) {
                SettingsRow(
                    header = stringResource(id = R.string.version),
                    text = stringResource(id = R.string.version_nr)
                )
            }
        }
    }
}

@Composable
private fun AccountRow(accountType: AccountType, onUpdate: OnUpdate) {
    val shortenedNpub = remember(accountType) { accountType.publicKey.shortenedBech32() }
    SettingsRow(
        imageVector = AccountIcon,
        header = when (accountType) {
            is ExternalAccount -> stringResource(id = R.string.external_signer)
            is DefaultAccount -> stringResource(id = R.string.default_account)
        },
        text = shortenedNpub,
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
                Text(text = stringResource(id = R.string.use_default_account))
            }

            is DefaultAccount -> TextButton(onClick = {
                onUpdate(RequestExternalAccount(context = context))
            }) {
                Text(text = stringResource(id = R.string.use_external_signer))
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

@Composable
private fun SettingsRow(
    header: String,
    imageVector: ImageVector? = null,
    text: String? = null,
    onClick: Fn = {},
    additionalContent: ComposableContent = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsBaseRow(header = header, imageVector = imageVector, text = text, onClick = onClick)
        additionalContent()
    }
}

@Composable
private fun SettingsBaseRow(
    header: String,
    imageVector: ImageVector? = null,
    text: String? = null,
    onClick: Fn = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageVector != null) {
            Icon(imageVector = imageVector, contentDescription = header)
            Spacer(modifier = Modifier.width(spacing.xl))
        }
        Column {
            Text(
                text = header,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(spacing.small))
            Text(text = text.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
