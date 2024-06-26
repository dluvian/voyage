package com.dluvian.voyage.ui.views.nonMain.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.getListTabHeaders
import com.dluvian.voyage.core.viewModel.ListViewModel
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.list.ProfileList
import com.dluvian.voyage.ui.components.list.TopicList
import kotlinx.coroutines.launch

@Composable
fun ListView(
    vm: ListViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    ListScaffold(
        title = vm.itemSetProvider.title.value,
        identifier = vm.itemSetProvider.identifier.value,
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ScreenContent(vm = vm)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenContent(vm: ListViewModel) {
    val scope = rememberCoroutineScope()
    val feedState = rememberLazyListState()
    val profileState = rememberLazyListState()
    val topicState = rememberLazyListState()
    SimpleTabPager(
        headers = getHeaders(
            numOfProfiles = vm.itemSetProvider.profiles.value.size,
            numOfTopics = vm.itemSetProvider.topics.value.size
        ),
        index = vm.tabIndex,
        pagerState = rememberPagerState { 3 },
        isLoading = vm.isLoading.value,
        onScrollUp = {
            when (it) {
                0 -> scope.launch { feedState.animateScrollToItem(0) }
                1 -> scope.launch { profileState.animateScrollToItem(0) }
                2 -> scope.launch { topicState.animateScrollToItem(0) }
                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> ComingSoon()

            1 -> ProfileList(
                profiles = vm.itemSetProvider.profiles.value,
                state = profileState,
            )

            2 -> TopicList(
                topics = vm.itemSetProvider.topics.value,
                state = topicState,
            )

            else -> ComingSoon()
        }
    }
}

@Composable
@Stable
private fun getHeaders(numOfProfiles: Int, numOfTopics: Int): List<String> {
    return listOf(stringResource(id = R.string.feed)) + getListTabHeaders(
        numOfProfiles = numOfProfiles,
        numOfTopics = numOfTopics
    )
}
