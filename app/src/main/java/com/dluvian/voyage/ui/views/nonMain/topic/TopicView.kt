package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.TopicViewNextPage
import com.dluvian.voyage.model.TopicViewRefresh
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.viewModel.TopicViewModel

@Composable
fun TopicView(vm: TopicViewModel, snackbar: SnackbarHostState, onUpdate: (Cmd) -> Unit) {
    TopicScaffold(vm = vm, snackbar = snackbar, onUpdate = onUpdate) {
        Feed(
            paginator = vm.paginator,
            state = vm.feedState,
            onRefresh = { onUpdate(TopicViewRefresh) },
            onAppend = { onUpdate(TopicViewNextPage) },
            onUpdate = onUpdate
        )
    }
}
