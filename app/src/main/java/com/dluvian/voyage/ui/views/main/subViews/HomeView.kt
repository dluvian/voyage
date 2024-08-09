package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.HomeViewAppend
import com.dluvian.voyage.core.HomeViewApplyFilter
import com.dluvian.voyage.core.HomeViewDismissFilter
import com.dluvian.voyage.core.HomeViewRefresh
import com.dluvian.voyage.core.HomeViewSubAccountAndTrustData
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.ListTopics
import com.dluvian.voyage.data.model.MyTopics
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.NoTopics
import com.dluvian.voyage.data.model.SingularPubkey
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.NamedCheckbox
import com.dluvian.voyage.ui.components.NamedRadio
import com.dluvian.voyage.ui.components.dialog.BaseActionDialog
import com.dluvian.voyage.ui.components.text.SmallHeader
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun HomeView(vm: HomeViewModel, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        onUpdate(HomeViewSubAccountAndTrustData)
    }

    Feed(
        paginator = vm.paginator,
        state = vm.feedState,
        onRefresh = { onUpdate(HomeViewRefresh) },
        onAppend = { onUpdate(HomeViewAppend) },
        onUpdate = onUpdate
    )

    if (vm.showFilterMenu.value) {
        val currentSetting = remember(vm.setting.value) { mutableStateOf(vm.setting.value) }
        BaseActionDialog(
            title = stringResource(id = R.string.filter),
            main = { Filter(setting = currentSetting) },
            onConfirm = { onUpdate(HomeViewApplyFilter(setting = currentSetting.value)) },
            onDismiss = { onUpdate(HomeViewDismissFilter) })
    }
}

@Composable
private fun Filter(setting: MutableState<HomeFeedSetting>) {
    Column {
        SmallHeader(header = stringResource(id = R.string.topics))
        NamedCheckbox(
            isChecked = when (setting.value.topicSelection) {
                MyTopics -> true
                is ListTopics, NoTopics -> false
            },
            name = stringResource(id = R.string.my_topics),
            onClick = {
                setting.value = when (setting.value.topicSelection) {
                    MyTopics -> setting.value.copy(topicSelection = NoTopics)
                    is ListTopics, NoTopics -> setting.value.copy(topicSelection = MyTopics)
                }
            })

        SmallHeader(
            modifier = Modifier.padding(top = spacing.small),
            header = stringResource(id = R.string.profiles)
        )
        NamedRadio(
            isSelected = when (setting.value.pubkeySelection) {
                NoPubkeys -> true
                FriendPubkeys, WebOfTrustPubkeys, Global -> false
                is CustomPubkeys, is ListPubkeys, is SingularPubkey -> false
            },
            name = stringResource(id = R.string.none),
            onClick = {
                setting.value = setting.value.copy(pubkeySelection = NoPubkeys)
            })
        NamedRadio(
            isSelected = when (setting.value.pubkeySelection) {
                FriendPubkeys -> true
                NoPubkeys, WebOfTrustPubkeys, Global -> false
                is CustomPubkeys, is ListPubkeys, is SingularPubkey -> false
            },
            name = stringResource(id = R.string.my_friends),
            onClick = {
                setting.value = setting.value.copy(pubkeySelection = FriendPubkeys)
            })
        NamedRadio(
            isSelected = when (setting.value.pubkeySelection) {
                WebOfTrustPubkeys -> true
                NoPubkeys, FriendPubkeys, Global -> false
                is CustomPubkeys, is ListPubkeys, is SingularPubkey -> false
            },
            name = stringResource(id = R.string.web_of_trust),
            onClick = {
                setting.value = setting.value.copy(pubkeySelection = WebOfTrustPubkeys)
            })
        NamedRadio(
            isSelected = when (setting.value.pubkeySelection) {
                Global -> true
                NoPubkeys, FriendPubkeys, WebOfTrustPubkeys -> false
                is CustomPubkeys, is ListPubkeys, is SingularPubkey -> false
            },
            name = stringResource(id = R.string.global),
            onClick = {
                setting.value = setting.value.copy(pubkeySelection = Global)
            })
    }
} 
