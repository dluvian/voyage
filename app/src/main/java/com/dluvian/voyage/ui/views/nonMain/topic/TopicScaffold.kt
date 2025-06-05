package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold
import com.dluvian.voyage.viewModel.TopicViewModel

@Composable
fun TopicScaffold(
    vm: TopicViewModel,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            TopicTopAppBar(
                topic = vm.currentTopic.value,
                isFollowed = vm.isFollowed.value,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
