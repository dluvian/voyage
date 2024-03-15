package com.dluvian.voyage.ui.views.nonMain.subViews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.SearchViewModel

@Composable
fun SearchView(vm: SearchViewModel, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        vm.subProfiles()
    }

}
