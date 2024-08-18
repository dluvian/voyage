package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.BookmarksViewAppend
import com.dluvian.voyage.core.BookmarksViewInit
import com.dluvian.voyage.core.BookmarksViewRefresh
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.viewModel.BookmarksViewModel
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold

@Composable
fun BookmarksView(vm: BookmarksViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    LaunchedEffect(key1 = Unit) {
        onUpdate(BookmarksViewInit)
    }
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.bookmarks),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        Feed(
            paginator = vm.paginator,
            postDetails = vm.postDetails,
            state = vm.feedState,
            onRefresh = { onUpdate(BookmarksViewRefresh) },
            onAppend = { onUpdate(BookmarksViewAppend) },
            onUpdate = onUpdate
        )
    }
}
