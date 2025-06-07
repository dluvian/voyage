package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dluvian.voyage.R
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.model.AddRelay
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.PublishNip65
import com.dluvian.voyage.model.RemoveRelay
import com.dluvian.voyage.model.ToggleReadRelay
import com.dluvian.voyage.model.ToggleWriteRelay
import com.dluvian.voyage.ui.components.ConnectionDot
import com.dluvian.voyage.ui.components.scaffold.SaveableScaffold
import com.dluvian.voyage.ui.components.selection.NamedCheckbox
import com.dluvian.voyage.ui.components.text.ClickableRelayUrl
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.DeleteIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.RelayEditorViewModel
import rust.nostr.sdk.Relay
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.RelayStatus

@Composable
fun RelayEditorView(
    vm: RelayEditorViewModel,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val nip65 by vm.nip65
    val poolRelays by vm.poolRelays
    val addIsEnabled by vm.addIsEnabled

    SaveableScaffold(
        showSaveButton = true,
        snackbar = snackbar,
        title = stringResource(id = R.string.relays),
        onSave = {
            onUpdate(PublishNip65(relays = nip65.toMap()))
        },
        onUpdate = onUpdate
    ) {
        RelayEditorViewContent(
            nip65 = nip65,
            poolRelays = poolRelays,
            addIsEnabled = addIsEnabled,
            state = vm.lazyListState,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun RelayEditorViewContent(
    nip65: List<Pair<RelayUrl, RelayMetadata?>>,
    poolRelays: Map<RelayUrl, Relay>,
    addIsEnabled: Boolean,
    state: LazyListState,
    onUpdate: (Cmd) -> Unit,
) {
    val poolList = remember(poolRelays) { poolRelays.values.toList() }

    LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
        item { SectionHeader(header = stringResource(id = R.string.my_relays)) }

        itemsIndexed(items = nip65) { index, (url, meta) ->
            MyRelayRow(
                relay = url,
                meta = meta,
                status = poolRelays[url]?.status() ?: RelayStatus.DISCONNECTED,
                isDeletable = nip65.size > 1,
                onUpdate = onUpdate,
            )
            if (index != nip65.size - 1) {
                HorizontalDivider()
            }
        }

        if (addIsEnabled) item {
            AddRelayRow(onUpdate)
            Spacer(modifier = Modifier.height(spacing.xxl))
        }

        addSection(
            titleId = R.string.relay_pool,
            relays = poolList,
            addIsEnabled = addIsEnabled,
            myRelays = nip65,
            showCount = true,
            onUpdate = onUpdate
        )

        addSection(
            titleId = R.string.popular_relays,
            relays = poolList,
            addIsEnabled = addIsEnabled,
            myRelays = nip65,
            onUpdate = onUpdate
        )
    }
}

private fun LazyListScope.addSection(
    titleId: Int,
    relays: List<Relay>,
    addIsEnabled: Boolean,
    myRelays: List<Pair<RelayUrl, RelayMetadata?>>,
    showCount: Boolean = false,
    onUpdate: (Cmd) -> Unit
) {
    if (relays.isNotEmpty()) {
        item { Spacer(modifier = Modifier.height(spacing.xxl)) }
        item {
            SectionHeader(
                header = stringResource(id = titleId)
                    .let { if (showCount) it + " (${relays.size})" else it }
            )
        }
        itemsIndexed(items = relays) { index, relay ->
            NormalRelayRow(
                relayUrl = relay.url(),
                isAddable = addIsEnabled && myRelays.none { (myUrl, _) -> myUrl == relay.url() },
                status = relay.status(),
                onUpdate = onUpdate,
            )
            if (index != relays.size - 1) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AddRelayRow(onUpdate: (Cmd) -> Unit) {
    val input = remember { mutableStateOf("") }
    val focus = LocalFocusManager.current

    val onDone = {
        onUpdate(AddRelay(input.value))
        input.value = ""
        focus.clearFocus()
    }

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = input.value,
        onValueChange = { input.value = it },
        placeholder = { Text(text = stringResource(id = R.string.add_relay)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            if (input.value.isNotBlank()) IconButton(onClick = onDone) {
                Icon(
                    imageVector = AddIcon,
                    contentDescription = stringResource(id = R.string.add_relay)
                )
            }
        })
}

@Composable
private fun NormalRelayRow(
    relayUrl: RelayUrl,
    isAddable: Boolean,
    status: RelayStatus?,
    onUpdate: (Cmd) -> Unit
) {
    RelayRow(relayUrl = relayUrl, status = status, onUpdate = onUpdate) {
        if (isAddable) IconButton(
            modifier = Modifier.size(sizing.relayActionButton),
            onClick = { onUpdate(AddRelay(relayUrl)) }
        ) {
            Icon(
                imageVector = AddIcon,
                contentDescription = stringResource(id = R.string.add_relay)
            )
        }
    }
}

@Composable
private fun MyRelayRow(
    relay: RelayUrl,
    meta: RelayMetadata?,
    status: RelayStatus,
    isDeletable: Boolean,
    onUpdate: (Cmd) -> Unit
) {
    RelayRow(
        relayUrl = relay,
        onUpdate = onUpdate,
        status = status,
        secondRow = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NamedCheckbox(
                    isChecked = meta == null || meta == RelayMetadata.READ,
                    name = stringResource(id = R.string.read),
                    isEnabled = meta != RelayMetadata.READ,
                    onClick = { onUpdate(ToggleReadRelay(relay)) }
                )
                Spacer(modifier = Modifier.width(spacing.xxl))
                NamedCheckbox(
                    isChecked = meta == null || meta == RelayMetadata.WRITE,
                    name = stringResource(id = R.string.write),
                    isEnabled = meta != RelayMetadata.WRITE,
                    onClick = { onUpdate(ToggleWriteRelay(relay)) }
                )
            }
        },
        trailingContent = {
            if (isDeletable) IconButton(
                modifier = Modifier.size(sizing.relayActionButton),
                onClick = { onUpdate(RemoveRelay(relay)) }) {
                Icon(
                    imageVector = DeleteIcon,
                    contentDescription = stringResource(id = R.string.remove_relay)
                )
            }
        }
    )
}

@Composable
private fun RelayRow(
    relayUrl: RelayUrl,
    onUpdate: (Cmd) -> Unit,
    status: RelayStatus?,
    secondRow: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.large)
            .padding(start = spacing.bigScreenEdge, end = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(weight = 1f, fill = false)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConnectionDot(status ?: RelayStatus.DISCONNECTED)
                    Spacer(modifier = Modifier.width(spacing.medium))
                ClickableRelayUrl(relayUrl = relayUrl, onUpdate = onUpdate)
            }
            secondRow()
        }
        trailingContent()
    }
}
