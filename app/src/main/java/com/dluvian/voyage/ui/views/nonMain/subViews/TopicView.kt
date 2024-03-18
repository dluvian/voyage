package com.dluvian.voyage.ui.views.nonMain.subViews

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.TopicViewAppend
import com.dluvian.voyage.core.TopicViewRefresh
import com.dluvian.voyage.core.viewModel.TopicViewModel
import com.dluvian.voyage.ui.components.Feed

@Composable
fun TopicView(vm: TopicViewModel, onUpdate: OnUpdate) {
    Feed(
        paginator = vm.paginator,
        onRefresh = { onUpdate(TopicViewRefresh) },
        onAppend = { onUpdate(TopicViewAppend) },
        onUpdate = onUpdate
    )
}
