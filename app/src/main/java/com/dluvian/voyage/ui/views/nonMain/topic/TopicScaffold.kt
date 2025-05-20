package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.TopicViewModel
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun TopicScaffold(
    vm: TopicViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: ComposableContent
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            TopicTopAppBar(
                topic = vm.currentTopic.value,
                isFollowed = vm.isFollowed.collectAsState().value,
                isMuted = vm.isMuted.collectAsState().value,
                addableLists = vm.addableLists.value,
                nonAddableLists = vm.nonAddableLists.value,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
