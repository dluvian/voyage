package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.NoPubkeys
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.HomeViewApplyFilter
import com.dluvian.voyage.model.HomeViewDismissFilter
import com.dluvian.voyage.model.HomeViewNextPage
import com.dluvian.voyage.model.HomeViewOpen
import com.dluvian.voyage.model.HomeViewRefresh
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.dialog.BaseActionDialog
import com.dluvian.voyage.ui.components.selection.FeedPubkeySelectionRadio
import com.dluvian.voyage.ui.components.selection.NamedCheckbox
import com.dluvian.voyage.ui.components.text.SmallHeader
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.HomeViewModel
import kotlinx.coroutines.launch
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard

@Composable
fun HomeView(vm: HomeViewModel, onUpdate: (Cmd) -> Unit) {
    LaunchedEffect(key1 = Unit) {
        onUpdate(HomeViewOpen)
    }

    Feed(
        paginator = vm.paginator,
        state = vm.feedState,
        onRefresh = { onUpdate(HomeViewRefresh) },
        onAppend = { onUpdate(HomeViewNextPage) },
        onUpdate = onUpdate
    )

    val scope = rememberCoroutineScope()
    if (vm.showFilterMenu.value) {
        val currentSetting = remember(vm.setting.value) { mutableStateOf(vm.setting.value) }
        BaseActionDialog(
            title = stringResource(id = R.string.filter),
            main = { Filter(setting = currentSetting) },
            onConfirm = {
                onUpdate(HomeViewApplyFilter(setting = currentSetting.value))
                scope.launch { vm.feedState.animateScrollToItem(index = 0) }
            },
            onDismiss = { onUpdate(HomeViewDismissFilter) })
    }
}

@Composable
private fun Filter(setting: MutableState<HomeFeedSetting>) {
    LazyColumn {
        item {
            SmallHeader(header = stringResource(id = R.string.topics))
            NamedCheckbox(
                isChecked = setting.value.withTopics,
                name = stringResource(id = R.string.my_topics),
                onClick = {
                    setting.value = when (setting.value.withTopics) {
                        true -> setting.value.copy(withTopics = false)
                        false -> setting.value.copy(withTopics = true)
                    }
                })
        }

        item {
            SmallHeader(
                modifier = Modifier.padding(top = spacing.small),
                header = stringResource(id = R.string.profiles)
            )
            FeedPubkeySelectionRadio(
                current = setting.value.pubkeySelection,
                target = NoPubkeys,
                onClick = { setting.value = setting.value.copy(pubkeySelection = NoPubkeys) })
            FeedPubkeySelectionRadio(
                current = setting.value.pubkeySelection,
                target = FriendPubkeys,
                onClick = {
                    setting.value = setting.value.copy(pubkeySelection = FriendPubkeys)
                })
            FeedPubkeySelectionRadio(
                current = setting.value.pubkeySelection,
                target = Global,
                onClick = { setting.value = setting.value.copy(pubkeySelection = Global) })
        }

        item {
            SmallHeader(header = stringResource(id = R.string.content))
            val hasKind1 =
                remember(setting) { setting.value.kinds.contains(Kind.fromStd(KindStandard.TEXT_NOTE)) }
            NamedCheckbox(
                isChecked = hasKind1,
                name = stringResource(id = R.string.posts_and_replies),
                onClick = {
                    val kinds = if (hasKind1) {
                        setting.value.kinds.filterNot { it.asStd() == KindStandard.TEXT_NOTE }
                    } else {
                        setting.value.kinds + Kind.fromStd(KindStandard.TEXT_NOTE)
                    }
                    setting.value = setting.value.copy(kinds = kinds)
                })
            val hasCrossPost =
                remember(setting) { setting.value.kinds.contains(Kind.fromStd(KindStandard.REPOST)) }
            NamedCheckbox(
                isChecked = hasCrossPost,
                name = stringResource(id = R.string.cross_posts),
                onClick = {
                    val kinds = if (hasCrossPost) {
                        setting.value.kinds.filterNot { it.asStd() == KindStandard.REPOST }
                    } else {
                        setting.value.kinds + Kind.fromStd(KindStandard.REPOST)
                    }
                    setting.value = setting.value.copy(kinds = kinds)
                })
        }
    }
}
