package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FollowListsViewRefresh
import com.dluvian.voyage.ui.components.list.ProfileAndTopicList
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.model.FollowableProfileItem
import com.dluvian.voyage.ui.model.FollowableTopicItem
import com.dluvian.voyage.viewModel.FollowListsViewModel

@Composable
fun FollowListsView(
    vm: FollowListsViewModel,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val isRefreshing by vm.isRefreshing
    val profilesRaw by vm.profiles
    val topicsRaw by vm.topics
    val profiles = remember(profilesRaw) {
        profilesRaw.map {
            FollowableProfileItem(profile = it, onUpdate = onUpdate)
        }
    }
    val topics = remember(topicsRaw) {
        topicsRaw.map { (topic, isFollowed) ->
            FollowableTopicItem(topic = topic, isFollowed = isFollowed, onUpdate = onUpdate)
        }
    }
    val headers = listOf(
        stringResource(id = R.string.profiles) + " (${profilesRaw.size})",
        stringResource(id = R.string.topics) + " (${topicsRaw.size})"
    )

    SimpleGoBackScaffold(
        header = stringResource(id = R.string.follow_lists),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ProfileAndTopicList(
            isRefreshing = isRefreshing,
            headers = headers,
            profiles = profiles,
            topics = topics,
            profileState = vm.contactListState,
            topicState = vm.topicListState,
            tabIndex = vm.tabIndex,
            pagerState = vm.pagerState,
            onRefresh = { onUpdate(FollowListsViewRefresh) },
        )
    }
}
