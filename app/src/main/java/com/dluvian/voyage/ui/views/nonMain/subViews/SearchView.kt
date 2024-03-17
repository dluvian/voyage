package com.dluvian.voyage.ui.views.nonMain.subViews

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.ui.components.ComingSoon

@Composable
fun SearchView(vm: SearchViewModel, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        vm.subProfiles()
    }
    ComingSoon()
    LazyColumn {
        items(vm.topics.value) {
            Text(it)
        }
        items(vm.profiles.value) {
            Text(it.name)
        }
    }
}
