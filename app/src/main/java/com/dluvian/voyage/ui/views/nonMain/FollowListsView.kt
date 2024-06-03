package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.FollowListsViewModel
import com.dluvian.voyage.ui.components.SimplePager
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowListsView(vm: FollowListsViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val headers = listOf(
        stringResource(id = R.string.contacts),
        stringResource(id = R.string.topics)
    )

    SimpleGoBackScaffold(
        header = stringResource(id = R.string.follow_lists),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        ScreenContent(headers = headers, index = vm.tabIndex, pagerState = vm.pagerState)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenContent(headers: List<String>, index: MutableIntState, pagerState: PagerState) {
    SimplePager(headers = headers, index = index, pagerState = pagerState) {
        when (it) {
            0 -> ComingSoon()
            1 -> ComingSoon()
            else -> ComingSoon()
        }
    }
}
