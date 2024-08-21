package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.InboxViewAppend
import com.dluvian.voyage.core.InboxViewApplyFilter
import com.dluvian.voyage.core.InboxViewDismissFilter
import com.dluvian.voyage.core.InboxViewInit
import com.dluvian.voyage.core.InboxViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.InboxViewModel
import com.dluvian.voyage.data.model.FriendPubkeysNoLock
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.dialog.BaseActionDialog
import com.dluvian.voyage.ui.components.selection.FeedPubkeySelectionRadio
import com.dluvian.voyage.ui.components.text.SmallHeader
import com.dluvian.voyage.ui.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun InboxView(vm: InboxViewModel, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        onUpdate(InboxViewInit)
    }

    Feed(
        paginator = vm.paginator,
        postDetails = vm.postDetails,
        state = vm.feedState,
        onRefresh = { onUpdate(InboxViewRefresh) },
        onAppend = { onUpdate(InboxViewAppend) },
        onUpdate = onUpdate
    )

    val scope = rememberCoroutineScope()
    if (vm.showFilterMenu.value) {
        val currentSetting = remember(vm.setting.value) { mutableStateOf(vm.setting.value) }
        BaseActionDialog(
            title = stringResource(id = R.string.filter),
            main = { Filter(setting = currentSetting) },
            onConfirm = {
                onUpdate(InboxViewApplyFilter(setting = currentSetting.value))
                scope.launch { vm.feedState.animateScrollToItem(index = 0) }
            },
            onDismiss = { onUpdate(InboxViewDismissFilter) })
    }
}

@Composable
private fun Filter(setting: MutableState<InboxFeedSetting>) {
    Column {
        SmallHeader(
            modifier = Modifier.padding(top = spacing.small),
            header = stringResource(id = R.string.profiles)
        )
        FeedPubkeySelectionRadio(
            current = setting.value.pubkeySelection,
            target = FriendPubkeysNoLock,
            onClick = { setting.value = setting.value.copy(pubkeySelection = FriendPubkeysNoLock) })
        FeedPubkeySelectionRadio(
            current = setting.value.pubkeySelection,
            target = WebOfTrustPubkeys,
            onClick = { setting.value = setting.value.copy(pubkeySelection = WebOfTrustPubkeys) })
        FeedPubkeySelectionRadio(
            current = setting.value.pubkeySelection,
            target = Global,
            onClick = { setting.value = setting.value.copy(pubkeySelection = Global) })
    }
}
