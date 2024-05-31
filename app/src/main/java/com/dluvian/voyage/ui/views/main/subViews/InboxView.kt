package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.dluvian.voyage.core.InboxViewAppend
import com.dluvian.voyage.core.InboxViewInit
import com.dluvian.voyage.core.InboxViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.InboxViewModel
import com.dluvian.voyage.ui.components.Feed

@Composable
fun InboxView(vm: InboxViewModel, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        onUpdate(InboxViewInit)
    }

    Feed(
        paginator = vm.paginator,
        state = vm.feedState,
        onRefresh = { onUpdate(InboxViewRefresh) },
        onAppend = { onUpdate(InboxViewAppend) },
        onUpdate = onUpdate
    )
}
