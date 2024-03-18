package com.dluvian.voyage.ui.views.main.subViews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.dluvian.voyage.core.HomeViewAppend
import com.dluvian.voyage.core.HomeViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.ui.components.Feed

@Composable
fun HomeView(vm: HomeViewModel, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        vm.subMyAccountAndTrustData()
    }

    Feed(
        paginator = vm.paginator,
        onRefresh = { onUpdate(HomeViewRefresh) },
        onAppend = { onUpdate(HomeViewAppend) },
        onUpdate = onUpdate
    )
}
