package com.dluvian.voyage.ui.views.nonMain.editList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.viewModel.EditListViewModel
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.button.RemoveIconButton
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.theme.HashtagIcon


private typealias ItemProps = Triple<ComposableContent, String, Fn>

@Composable
fun EditListView(vm: EditListViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val headers = listOf(
        stringResource(id = R.string.profiles),
        stringResource(id = R.string.topics)
    )
    val profilesRaw by vm.profiles
    val topicsRaw by vm.topics
    val profiles = remember(profilesRaw) {
        profilesRaw.mapIndexed { i, profile ->
            ItemProps(
                first = {
                    TrustIcon(
                        trustType = TrustType.from(
                            isOneself = profile.isMe,
                            isFriend = profile.isFriend,
                            isWebOfTrust = profile.isWebOfTrust
                        )
                    )
                },
                second = profile.name,
                third = { vm.profiles.value = vm.profiles.value.drop(i) },
            )
        }
    }
    val topics = remember(topicsRaw) {
        topicsRaw.mapIndexed { i, topic ->
            ItemProps(
                first = { Icon(imageVector = HashtagIcon, contentDescription = null) },
                second = topic,
                third = { vm.topics.value = vm.topics.value.drop(i) }
            )
        }
    }

    EditListScaffold(title = vm.title, snackbar = snackbar, onUpdate = onUpdate) {
        ScreenContent(headers = headers, profiles = profiles, topics = topics, vm = vm)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenContent(
    headers: List<String>,
    profiles: List<ItemProps>,
    topics: List<ItemProps>,
    vm: EditListViewModel
) {
    SimpleTabPager(
        headers = headers,
        index = vm.tabIndex,
        pagerState = vm.pagerState,
        onScrollUp = {
            when (it) {
                0 -> {
                    TODO()
                }

                1 -> {
                    TODO()
                }

                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> ItemList(items = profiles)
            1 -> ItemList(items = topics)

            else -> ComingSoon()
        }
    }
}

@Composable
private fun ItemList(items: List<ItemProps>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { (icon, label, onRemove) ->
            ClickableRow(
                header = label,
                leadingContent = icon,
                trailingContent = {
                    RemoveIconButton(onRemove = onRemove)
                },
                onClick = { }
            )
        }
    }
}
