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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.R
import com.dluvian.voyage.core.LoadRelays
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.RelayEditorViewModel
import com.dluvian.voyage.ui.components.scaffold.SaveableScaffold
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.DeleteIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun RelayEditorView(vm: RelayEditorViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val myRelays by vm.myRelays
    val popularRelays by vm.popularRelays
    val addIsEnabled by vm.addIsEnabled
    val isSaving by vm.isSaving

    LaunchedEffect(key1 = Unit) {
        onUpdate(LoadRelays)
    }

    SaveableScaffold(
        showSaveButton = true,
        isSaving = isSaving,
        snackbar = snackbar,
        title = stringResource(id = R.string.relays),
        onSave = {
        },
        onUpdate = onUpdate
    ) {
        RelayEditorViewContent(
            myRelays = myRelays,
            popularRelays = popularRelays,
            addIsEnabled = addIsEnabled,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun RelayEditorViewContent(
    myRelays: List<Nip65Relay>,
    popularRelays: List<RelayUrl>,
    addIsEnabled: Boolean,
    onUpdate: OnUpdate,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { SectionHeader(header = stringResource(id = R.string.my_relays)) }

        itemsIndexed(items = myRelays) { index, relay ->
            MyRelayRow(
                relay = relay,
                isDeletable = myRelays.size > 1,
                onUpdate = onUpdate,
            )
            if (index != myRelays.size - 1) {
                HorizontalDivider()
            }
        }


        if (addIsEnabled) item {
            AddRelayRow(onUpdate = onUpdate)
            Spacer(modifier = Modifier.height(spacing.xxl))
        }

        if (popularRelays.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(spacing.xxl)) }
            item { SectionHeader(header = stringResource(id = R.string.popular_relays)) }
            itemsIndexed(items = popularRelays) { index, relayUrl ->
                PopularRelayRow(
                    relayUrl = relayUrl,
                    isAddable = addIsEnabled && myRelays.none { it.url == relayUrl },
                    onUpdate = onUpdate,
                )
                if (index != popularRelays.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AddRelayRow(onUpdate: OnUpdate) {
    val input = remember { mutableStateOf("") }
    val focus = LocalFocusManager.current
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = input.value,
        onValueChange = { input.value = it },
        placeholder = { Text(text = stringResource(id = R.string.add_relay)) },
        trailingIcon = {
            if (input.value.isNotBlank()) IconButton(
                onClick = {
                    input.value = ""
                    focus.clearFocus()
                }) {
                Icon(
                    imageVector = AddIcon,
                    contentDescription = stringResource(id = R.string.add_relay)
                )
            }
        })
}

@Composable
private fun PopularRelayRow(relayUrl: RelayUrl, isAddable: Boolean, onUpdate: OnUpdate) {
    RelayRow(relayUrl = relayUrl, onUpdate = onUpdate) {
        if (isAddable) IconButton(modifier = Modifier.size(sizing.iconButton), onClick = {}) {
            Icon(imageVector = AddIcon, contentDescription = "add relay")
        }
    }
}

@Composable
private fun MyRelayRow(
    relay: Nip65Relay,
    isDeletable: Boolean,
    onUpdate: OnUpdate
) {
    RelayRow(
        relayUrl = relay.url,
        onUpdate = onUpdate,
        secondRow = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("lol")
            }
        },
        trailingContent = {
            if (isDeletable) IconButton(
                modifier = Modifier.size(sizing.iconButton),
                onClick = { /*TODO*/ }) {
                Icon(imageVector = DeleteIcon, contentDescription = "Delete relay")
            }
        }
    )
}

@Composable
private fun RelayRow(
    relayUrl: RelayUrl,
    onUpdate: OnUpdate,
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
            MainRelayRow(relay = relayUrl, onUpdate = onUpdate)
            secondRow()
        }
        trailingContent()
    }
}

@Composable
fun MainRelayRow(relay: RelayUrl, onUpdate: OnUpdate) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = relay)
    }
}
