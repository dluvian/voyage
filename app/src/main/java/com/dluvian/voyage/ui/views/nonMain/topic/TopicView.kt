package com.dluvian.voyage.ui.views.nonMain.topic

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.TopicViewAppend
import com.dluvian.voyage.core.TopicViewRefresh
import com.dluvian.voyage.core.viewModel.TopicViewModel
import com.dluvian.voyage.ui.components.Feed

@Composable
fun TopicView(vm: TopicViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    TopicScaffold(vm = vm, snackbar = snackbar, onUpdate = onUpdate) {
        Feed(
            paginator = vm.paginator,
            postDetails = vm.postDetails,
            state = vm.feedState,
            onRefresh = { onUpdate(TopicViewRefresh) },
            onAppend = { onUpdate(TopicViewAppend) },
            onUpdate = onUpdate
        )
    }
}
