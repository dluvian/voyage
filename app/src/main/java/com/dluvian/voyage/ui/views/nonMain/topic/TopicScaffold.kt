package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val topic by vm.currentTopic
    val isFollowed by vm.isFollowed.collectAsState()

    VoyageScaffold(
        snackbar = snackbar,
        topBar = { TopicTopAppBar(topic = topic, isFollowed = isFollowed, onUpdate = onUpdate) }
    ) {
        content()
    }
}
