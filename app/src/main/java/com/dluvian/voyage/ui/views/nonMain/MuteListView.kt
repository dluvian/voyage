package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.MuteListViewOpen
import com.dluvian.voyage.core.MuteListViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.MuteListViewModel
import com.dluvian.voyage.ui.components.list.ProfileAndTopicList
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.model.MutableProfileItem
import com.dluvian.voyage.ui.model.MutableTopicItem

@Composable
fun MuteListView(vm: MuteListViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val isRefreshing by vm.isRefreshing
    val mutedProfilesRaw by vm.mutedProfiles.value.collectAsState()
    val mutedTopicsRaw by vm.mutedTopics.value.collectAsState()
    val mutedProfiles = remember(mutedProfilesRaw) {
        mutedProfilesRaw.map {
            MutableProfileItem(profile = it, onUpdate = onUpdate)
        }
    }
    val mutedTopics = remember(mutedTopicsRaw) {
        mutedTopicsRaw.map {
            MutableTopicItem(topic = it.topic, isMuted = it.isMuted, onUpdate = onUpdate)
        }
    }
    val headers = listOf(
        stringResource(id = R.string.profiles) + " (${mutedProfilesRaw.size})",
        stringResource(id = R.string.topics) + " (${mutedTopicsRaw.size})",
        stringResource(id = R.string.words) + " (${vm.mutedWords.value.size})"
    )

    LaunchedEffect(key1 = Unit) {
        onUpdate(MuteListViewOpen)
    }

    SimpleGoBackScaffold(
        header = stringResource(id = R.string.mute_list),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ProfileAndTopicList(
            isRefreshing = isRefreshing,
            headers = headers,
            profiles = mutedProfiles,
            topics = mutedTopics,
            words = vm.mutedWords,
            profileState = vm.mutedProfileState,
            topicState = vm.mutedTopicState,
            wordState = vm.mutedWordState,
            tabIndex = vm.tabIndex,
            pagerState = vm.pagerState,
            onRefresh = { onUpdate(MuteListViewRefresh) },
            onUpdate = onUpdate,
        )
    }
}
