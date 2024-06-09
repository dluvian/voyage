package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.FollowListsViewInit
import com.dluvian.voyage.core.FollowListsViewRefresh
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.viewModel.FollowListsViewModel
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.button.FollowButton
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.model.Followable
import com.dluvian.voyage.ui.theme.HashtagIcon
import kotlinx.coroutines.launch

@Composable
fun FollowListsView(vm: FollowListsViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val profilesRaw by vm.profiles.value.collectAsState()
    val topicsRaw by vm.topics.value.collectAsState()
    val contacts = remember(profilesRaw) {
        profilesRaw.map {
            Followable(
                label = it.name,
                isFollowed = it.isFriend,
                icon = {
                    TrustIcon(
                        trustType = TrustType.from(
                            isOneself = it.isMe,
                            isFriend = it.isFriend,
                            isWebOfTrust = it.isWebOfTrust
                        )
                    )
                },
                onFollow = { onUpdate(FollowProfile(pubkey = it.pubkey)) },
                onUnfollow = { onUpdate(UnfollowProfile(pubkey = it.pubkey)) },
                onOpen = { onUpdate(OpenProfile(nprofile = it.toNip19())) }
            )
        }
    }
    val topics = remember(topicsRaw) {
        topicsRaw.map {
            Followable(
                label = it.topic,
                isFollowed = it.isFollowed,
                icon = {
                    Icon(imageVector = HashtagIcon, contentDescription = null)
                },
                onFollow = { onUpdate(FollowTopic(topic = it.topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic = it.topic)) },
                onOpen = { onUpdate(OpenTopic(topic = it.topic)) },
            )
        }
    }
    val headers = listOf(
        stringResource(id = R.string.profiles) + " (${profilesRaw.size})",
        stringResource(id = R.string.topics) + " (${topicsRaw.size})"
    )

    LaunchedEffect(key1 = Unit) {
        onUpdate(FollowListsViewInit)
    }

    SimpleGoBackScaffold(
        header = stringResource(id = R.string.follow_lists),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ScreenContent(
            isRefreshing = isRefreshing,
            headers = headers,
            contacts = contacts,
            topics = topics,
            vm = vm,
            onUpdate = onUpdate
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenContent(
    isRefreshing: Boolean,
    headers: List<String>,
    contacts: List<Followable>,
    topics: List<Followable>,
    vm: FollowListsViewModel,
    onUpdate: OnUpdate
) {
    val scope = rememberCoroutineScope()
    SimpleTabPager(
        headers = headers,
        index = vm.tabIndex,
        pagerState = vm.pagerState,
        onScrollUp = {
            when (it) {
                0 -> scope.launch { vm.contactListState.animateScrollToItem(0) }
                1 -> scope.launch { vm.topicListState.animateScrollToItem(0) }
                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> GenericList(
                followable = contacts,
                isRefreshing = isRefreshing,
                hintIfEmpty = stringResource(id = R.string.your_contact_list_is_empty),
                state = vm.contactListState,
                onUpdate = onUpdate
            )

            1 -> GenericList(
                followable = topics,
                isRefreshing = isRefreshing,
                hintIfEmpty = stringResource(id = R.string.your_topic_list_is_empty),
                state = vm.topicListState,
                onUpdate = onUpdate
            )

            else -> ComingSoon()
        }
    }
}

@Composable
private fun GenericList(
    followable: List<Followable>,
    isRefreshing: Boolean,
    hintIfEmpty: String,
    state: LazyListState,
    onUpdate: OnUpdate
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(FollowListsViewRefresh) }) {
        if (followable.isEmpty()) BaseHint(text = hintIfEmpty)
        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            items(followable) {
                ClickableRow(
                    header = it.label,
                    leadingContent = it.icon,
                    trailingContent = {
                        FollowButton(
                            isFollowed = it.isFollowed,
                            onFollow = it.onFollow,
                            onUnfollow = it.onUnfollow
                        )
                    },
                    onClick = it.onOpen
                )
            }
        }
    }
}
