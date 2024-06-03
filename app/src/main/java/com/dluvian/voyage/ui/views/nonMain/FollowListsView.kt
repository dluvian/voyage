package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.dluvian.voyage.ui.components.SimplePager
import com.dluvian.voyage.ui.components.chip.FollowChip
import com.dluvian.voyage.ui.components.icon.TrustIcon
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.model.Followable
import com.dluvian.voyage.ui.theme.HashtagIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun FollowListsView(vm: FollowListsViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val contactsRaw by vm.contacts.value.collectAsState()
    val topicsRaw by vm.topics.value.collectAsState()
    val contacts = remember(contactsRaw) {
        contactsRaw.map {
            Followable(
                label = it.inner.name,
                isFollowed = it.inner.isFriend,
                icon = {
                    TrustIcon(
                        trustType = TrustType.from(
                            isOneself = it.inner.isMe,
                            isFriend = it.inner.isFriend,
                            isWebOfTrust = it.inner.isWebOfTrust
                        )
                    )
                },
                onFollow = { onUpdate(FollowProfile(pubkey = it.inner.pubkey)) },
                onUnfollow = { onUpdate(UnfollowProfile(pubkey = it.inner.pubkey)) },
                onOpen = { onUpdate(OpenProfile(nprofile = it.inner.toNip19())) }
            )
        }
    }
    val topics = remember(topicsRaw) {
        topicsRaw.map {
            Followable(
                label = it.topic,
                isFollowed = it.isFollowed,
                icon = {
                    Icon(
                        modifier = Modifier.padding(start = spacing.large),
                        imageVector = HashtagIcon,
                        contentDescription = it.topic
                    )
                },
                onFollow = { onUpdate(FollowTopic(topic = it.topic)) },
                onUnfollow = { onUpdate(UnfollowTopic(topic = it.topic)) },
                onOpen = { onUpdate(OpenTopic(topic = it.topic)) },
            )
        }
    }
    val headers = listOf(
        stringResource(id = R.string.contacts) + " (${contactsRaw.size})",
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
    SimplePager(headers = headers, index = vm.tabIndex, pagerState = vm.pagerState) {
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
                FollowChip(item = it, fillWidth = true)
            }
        }
    }
}
