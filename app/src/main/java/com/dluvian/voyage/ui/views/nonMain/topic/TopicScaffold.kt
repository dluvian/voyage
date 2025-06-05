package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold
import com.dluvian.voyage.viewModel.TopicViewModel

Composable () ->Unit
import com.dluvian.voyage.core.(

)->Unit
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold
import com.dluvian.voyage.viewModel.TopicViewModel

@Composable
fun TopicScaffold(
    vm: TopicViewModel,
    snackbar: SnackbarHostState,
    onUpdate: () -> Unit,
    content: @Composable () -> Unit
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            TopicTopAppBar(
                topic = vm.currentTopic.value,
                isFollowed = vm.isFollowed.collectAsState().value,
                addableLists = vm.addableLists.value,
                nonAddableLists = vm.nonAddableLists.value,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
