package com.dluvian.voyage.ui.views.nonMain.editList

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.EditListViewAddProfile
import com.dluvian.voyage.EditListViewAddTopic
import com.dluvian.voyage.R
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.getListTabHeaders
import com.dluvian.voyage.getTransparentTextFieldColors
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.dialog.AddProfileDialog
import com.dluvian.voyage.ui.components.dialog.AddTopicDialog
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.list.ProfileList
import com.dluvian.voyage.ui.components.list.TopicList
import com.dluvian.voyage.ui.components.row.AddRow
import com.dluvian.voyage.viewModel.EditListViewModel
import kotlinx.coroutines.launch


@Composable
fun EditListView(
    vm: EditListViewModel,
    profileSuggestions: State<List<AdvancedProfileView>>,
    topicSuggestions: State<List<Topic>>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    EditListScaffold(
        title = vm.title,
        isSaving = vm.isSaving.value,
        hasItems = vm.topics.value.isNotEmpty() || vm.profiles.value.isNotEmpty(),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ScreenContent(
            profileSuggestions = profileSuggestions.value,
            topicSuggestions = topicSuggestions.value,
            vm = vm,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun ScreenContent(
    profileSuggestions: List<AdvancedProfileView>,
    topicSuggestions: List<Topic>,
    vm: EditListViewModel,
    onUpdate: OnUpdate
) {
    val profileState = rememberLazyListState()
    val topicState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showProfileDialog = remember { mutableStateOf(false) }
    val showTopicDialog = remember { mutableStateOf(false) }
    if (showProfileDialog.value) {
        AddProfileDialog(
            profileSuggestions = profileSuggestions,
            onAdd = {
                onUpdate(EditListViewAddProfile(profile = it))
                showProfileDialog.value = false
            },
            onDismiss = { showProfileDialog.value = false },
            onUpdate = onUpdate
        )
    }
    if (showTopicDialog.value) {
        AddTopicDialog(
            topicSuggestions = topicSuggestions,
            showNext = true,
            onAdd = { onUpdate(EditListViewAddTopic(topic = it)) },
            onDismiss = { showTopicDialog.value = false },
            onUpdate = onUpdate
        )
    }
    SimpleTabPager(
        headers = getListTabHeaders(
            numOfProfiles = vm.profiles.value.size,
            numOfTopics = vm.topics.value.size
        ) + stringResource(id = R.string.about),
        index = vm.tabIndex,
        pagerState = rememberPagerState { 3 },
        isLoading = vm.isLoading.value,
        onScrollUp = {
            when (it) {
                0 -> scope.launch { profileState.animateScrollToItem(0) }
                1 -> scope.launch { topicState.animateScrollToItem(0) }
                else -> {}
            }
        },
    ) {
        when (it) {
            0 -> ProfileList(
                profiles = vm.profiles.value,
                state = profileState,
                isRemovable = true,
                firstRow = {
                    if (vm.profiles.value.size < MAX_KEYS_SQL) AddRow(
                        header = stringResource(id = R.string.add_profile),
                        onClick = { showProfileDialog.value = true })
                },
                onRemove = { i ->
                    vm.profiles.value = vm.profiles.value
                        .toMutableList()
                        .apply { removeAt(i) }
                }
            )

            1 -> TopicList(
                topics = vm.topics.value,
                state = topicState,
                isRemovable = true,
                firstRow = {
                    if (vm.topics.value.size < MAX_KEYS_SQL) AddRow(
                        header = stringResource(id = R.string.add_topic),
                        onClick = { showTopicDialog.value = true })
                },
                onRemove = { i ->
                    vm.topics.value = vm.topics.value
                        .toMutableList()
                        .apply { removeAt(i) }
                }
            )

            2 -> TextField(
                modifier = Modifier.fillMaxSize(),
                value = vm.description.value,
                onValueChange = { newVal -> vm.description.value = newVal },
                colors = getTransparentTextFieldColors(),
                label = { Text(text = stringResource(R.string.description)) },
                placeholder = { Text(text = stringResource(id = R.string.describe_this_list)) })

            else -> ComingSoon()
        }
    }
}
