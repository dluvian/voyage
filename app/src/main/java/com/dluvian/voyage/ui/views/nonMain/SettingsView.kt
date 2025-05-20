package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dluvian.voyage.R
import com.dluvian.voyage.core.AddClientTag
import com.dluvian.voyage.core.ChangeUpvoteContent
import com.dluvian.voyage.core.ClickCreateGitIssue
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.DeleteAllPosts
import com.dluvian.voyage.core.ExportDatabase
import com.dluvian.voyage.core.LoadSeed
import com.dluvian.voyage.core.LockAccount
import com.dluvian.voyage.core.MAX_AUTOPILOT_RELAYS
import com.dluvian.voyage.core.MAX_RETAIN_ROOT
import com.dluvian.voyage.core.MIN_AUTOPILOT_RELAYS
import com.dluvian.voyage.core.MIN_RETAIN_ROOT
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.RebroadcastMyLockEvent
import com.dluvian.voyage.core.RequestExternalAccount
import com.dluvian.voyage.core.SendAuth
import com.dluvian.voyage.core.SendBookmarkedToLocalRelay
import com.dluvian.voyage.core.SendUpvotedToLocalRelay
import com.dluvian.voyage.core.ShowUsernames
import com.dluvian.voyage.core.UpdateAutopilotRelays
import com.dluvian.voyage.core.UpdateLocalRelayPort
import com.dluvian.voyage.core.UpdateRootPostThreshold
import com.dluvian.voyage.core.UseDefaultAccount
import com.dluvian.voyage.core.UseV2Replies
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import com.dluvian.voyage.core.model.ExternalAccount
import com.dluvian.voyage.core.model.Locked
import com.dluvian.voyage.core.utils.toShortenedNpub
import com.dluvian.voyage.core.utils.toTextFieldValue
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.data.nostr.LOCAL_WEBSOCKET
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.ui.components.bottomSheet.SeedBottomSheet
import com.dluvian.voyage.ui.components.dialog.BaseActionDialog
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.indicator.SmallCircleProgressIndicator
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.components.text.AltSectionHeader
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.WarningIcon
import com.dluvian.voyage.ui.theme.getTrustColor
import com.dluvian.voyage.ui.theme.spacing
import kotlinx.coroutines.CoroutineScope

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
    val scope = rememberCoroutineScope()
    val isLocked = vm.isLocked.collectAsState()
    LazyColumn {
        if (vm.isLoadingAccount.value) item { FullLinearProgressIndicator() }
        item {
            AccountSection(
                accountType = vm.accountType.value,
                seed = vm.seed.value,
                isLocking = vm.isLocking.value,
                isLocked = vm.isLockedForced.value || isLocked.value,
                scope = scope,
                onUpdate = onUpdate
            )
        }
        item {
            RelaySection(vm = vm, onUpdate = onUpdate)
        }
        item {
            DatabaseSection(vm = vm, scope = scope, onUpdate = onUpdate)
        }
        item {
            AppSection(vm = vm, onUpdate = onUpdate)
        }
    }
}

@Composable
private fun AccountSection(
    accountType: AccountType,
    seed: List<String>,
    isLocking: Boolean,
    isLocked: Boolean,
    scope: CoroutineScope,
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

        val showLockDialog = remember { mutableStateOf(false) }
        if (showLockDialog.value) BaseActionDialog(
            title = stringResource(id = R.string.lock_your_account),
            icon = WarningIcon,
            iconTint = getTrustColor(trustType = Locked),
            text = stringResource(id = R.string.lock_your_account_warning),
            confirmText = stringResource(id = R.string.lock_my_account),
            onConfirm = {
                onUpdate(LockAccount(uiScope = scope))
                showLockDialog.value = false
            },
            onDismiss = { showLockDialog.value = false })
        if (!isLocked) ClickableRow(
            header = stringResource(id = R.string.lock_your_account),
            text = stringResource(id = R.string.lock_your_account_in_case_your_keys_are_compromised),
            trailingContent = { if (isLocking) SmallCircleProgressIndicator() },
            onClick = { showLockDialog.value = true })
        else ClickableRow(
            header = stringResource(id = R.string.rebroadcast_your_lock_event),
            text = stringResource(id = R.string.your_account_is_locked_click_to_rebroadcast),
            onClick = { onUpdate(RebroadcastMyLockEvent(uiScope = scope)) })
    }
}

@Composable
private fun RelaySection(vm: SettingsViewModel, onUpdate: OnUpdate) {
    val focusRequester = remember { FocusRequester() }

    SettingsSection(header = stringResource(id = R.string.relays)) {
        val showAutopilotDialog = remember { mutableStateOf(false) }
        if (showAutopilotDialog.value) {
            val newVal = remember { mutableFloatStateOf(vm.autopilotRelays.intValue.toFloat()) }
            BaseActionDialog(
                title = stringResource(id = R.string.max_relays) + ": ${newVal.floatValue.toInt()}",
                main = {
                    Slider(
                        modifier = Modifier.padding(horizontal = spacing.bigScreenEdge),
                        value = newVal.floatValue,
                        onValueChange = { newVal.floatValue = it },
                        valueRange = MIN_AUTOPILOT_RELAYS.toFloat()..MAX_AUTOPILOT_RELAYS.toFloat()
                    )
                },
                onConfirm = {
                    onUpdate(UpdateAutopilotRelays(numberOfRelays = newVal.floatValue.toInt()))
                },
                onDismiss = { showAutopilotDialog.value = false })
        }
        ClickableRow(
            header = stringResource(id = R.string.max_autopilot_relays) + ": ${vm.autopilotRelays.intValue}",
            text = stringResource(id = R.string.max_num_of_relays_autopilot_is_allowed_to_select),
            onClick = { showAutopilotDialog.value = true })

        val showPortDialog = remember { mutableStateOf(false) }
        if (showPortDialog.value) {
            val newPort = remember {
                mutableStateOf(vm.localRelayPort.value?.toString().orEmpty().toTextFieldValue())
            }
            val parsedNewPort = remember(newPort.value) {
                runCatching { newPort.value.text.toUShort() }.getOrNull()
            }
            BaseActionDialog(title = stringResource(id = R.string.local_relay_port),
                main = {
                    LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester = focusRequester),
                        value = newPort.value,
                        prefix = { Text(text = LOCAL_WEBSOCKET) },
                        onValueChange = { newStr ->
                            if (newStr.text.length <= 5 &&
                                newStr.text.all { it.isDigit() } &&
                                !newStr.text.startsWith("0")
                            ) {
                                newPort.value = newStr
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        ),
                    )
                },
                confirmIsEnabled = newPort.value.text.isEmpty() || parsedNewPort != null,
                onConfirm = { onUpdate(UpdateLocalRelayPort(port = parsedNewPort)) },
                onDismiss = { showPortDialog.value = false })
        }
        val headerSuffix = remember(vm.localRelayPort.value) {
            vm.localRelayPort.value.let { if (it != null) ": $it" else "" }
        }
        ClickableRow(header = stringResource(id = R.string.local_relay_port) + headerSuffix,
            text = stringResource(id = R.string.port_number_of_your_local_relay),
            onClick = { showPortDialog.value = true })

        ClickableRowCheckbox(
            header = stringResource(id = R.string.authenticate_via_auth),
            text = stringResource(id = R.string.enable_to_authenticate_yourself_to_relays),
            checked = vm.sendAuth.value,
            onClickChange = { onUpdate(SendAuth(sendAuth = it)) })

        ClickableRowCheckbox(
            header = stringResource(id = R.string.send_bookmarked_post_to_local_relay),
            text = stringResource(id = R.string.send_post_to_local_relay_after_bookmarking_it),
            checked = vm.sendBookmarkedToLocalRelay.value,
            onClickChange = { onUpdate(SendBookmarkedToLocalRelay(sendToLocalRelay = it)) })

        ClickableRowCheckbox(
            header = stringResource(id = R.string.send_upvoted_post_to_local_relay),
            text = stringResource(id = R.string.send_post_to_local_relay_after_upvoting_it),
            checked = vm.sendUpvotedToLocalRelay.value,
            onClickChange = { onUpdate(SendUpvotedToLocalRelay(it)) })
    }
}

@Composable
private fun DatabaseSection(
    vm: SettingsViewModel,
    scope: CoroutineScope,
    onUpdate: OnUpdate
) {
    SettingsSection(header = stringResource(id = R.string.database)) {
        val showThresholdDialog = remember { mutableStateOf(false) }
        if (showThresholdDialog.value) {
            val newNum = remember { mutableFloatStateOf(vm.rootPostThreshold.intValue.toFloat()) }
            BaseActionDialog(title = stringResource(id = R.string.threshold) + ": ${newNum.floatValue.toInt()}",
                main = {
                    Slider(
                        modifier = Modifier.padding(horizontal = spacing.bigScreenEdge),
                        value = newNum.floatValue,
                        onValueChange = { newNum.floatValue = it },
                        valueRange = MIN_RETAIN_ROOT..MAX_RETAIN_ROOT
                    )
                },
                onConfirm = {
                    onUpdate(UpdateRootPostThreshold(threshold = newNum.floatValue))
                },
                onDismiss = { showThresholdDialog.value = false })
        }
        ClickableRow(header = stringResource(
            id = R.string.keep_at_least_n_root_posts, vm.rootPostThreshold.intValue
        ), text = stringResource(
            id = R.string.currently_n_root_posts_in_db,
            vm.currentRootPostCount.collectAsState().value
        ), onClick = { showThresholdDialog.value = true })

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
            })

        val isDeleting = vm.isDeleting.value
        val showDeleteDialog = remember { mutableStateOf(false) }
        if (showDeleteDialog.value) BaseActionDialog(
            title = stringResource(id = R.string.delete_posts),
            text = stringResource(id = R.string.are_you_sure_you_want_to_delete_all_posts_from_the_database),
            confirmText = stringResource(id = R.string.delete),
            onConfirm = { onUpdate(DeleteAllPosts(uiScope = scope)) },
            onDismiss = { showDeleteDialog.value = false })
        ClickableRow(
            header = stringResource(id = R.string.delete_posts),
            text = stringResource(id = R.string.remove_all_posts_from_database),
            onClick = { showDeleteDialog.value = true },
            trailingContent = {
                if (isDeleting) SmallCircleProgressIndicator()
            })
    }
}

@Composable
private fun AppSection(vm: SettingsViewModel, onUpdate: OnUpdate) {
    val focusRequester = remember { FocusRequester() }

    SettingsSection(header = stringResource(id = R.string.app)) {

        ClickableRowCheckbox(
            header = stringResource(id = R.string.show_usernames_in_feed),
            text = stringResource(id = R.string.show_the_authors_username_in_feeds),
            checked = vm.showUsernames.value,
            onClickChange = { onUpdate(ShowUsernames(showUsernames = it)) })

        val showUpvoteDialog = remember { mutableStateOf(false) }
        if (showUpvoteDialog.value) {
            val newUpvote = remember { mutableStateOf(vm.currentUpvote.value.toTextFieldValue()) }
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

        ClickableRowCheckbox(
            header = stringResource(id = R.string.always_use_v2_replies),
            text = stringResource(id = R.string.this_format_is_not_widely_adopted_yet),
            checked = vm.useV2Replies.value,
            onClickChange = { onUpdate(UseV2Replies(useV2Replies = it)) })

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
