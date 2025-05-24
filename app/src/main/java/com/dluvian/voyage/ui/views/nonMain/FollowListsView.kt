package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.FollowListsViewInit
import com.dluvian.voyage.core.FollowListsViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.FollowListsViewModel
import com.dluvian.voyage.ui.components.list.ProfileAndTopicList
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.model.FollowableProfileItem
import com.dluvian.voyage.ui.model.FollowableTopicItem

@Composable
fun FollowListsView(vm: FollowListsViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val profilesRaw by vm.profiles.value.collectAsState()
    val topicsRaw by vm.topics.value.collectAsState()
    val contacts = remember(profilesRaw) {
        profilesRaw.map {
            FollowableProfileItem(profile = it, onUpdate = onUpdate)
        }
    }
    val topics = remember(topicsRaw) {
        topicsRaw.map {
            FollowableTopicItem(topic = it.topic, isFollowed = it.isFollowed, onUpdate = onUpdate)
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
        ProfileAndTopicList(
            isRefreshing = isRefreshing,
            headers = headers,
            profiles = contacts,
            topics = topics,
            profileState = vm.contactListState,
            topicState = vm.topicListState,
            tabIndex = vm.tabIndex,
            pagerState = vm.pagerState,
            onRefresh = { onUpdate(FollowListsViewRefresh) },
        )
    }
}
