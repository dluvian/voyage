package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.BookmarkViewNextPage
import com.dluvian.voyage.model.BookmarkViewRefresh
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.viewModel.BookmarkViewModel

@Composable
fun BookmarkView(vm: BookmarkViewModel, snackbar: SnackbarHostState, onUpdate: (Cmd) -> Unit) {
    SimpleGoBackScaffold(
        header = stringResource(id = R.string.bookmarks),
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        Feed(
            paginator = vm.paginator,
            state = vm.feedState,
            onRefresh = { onUpdate(BookmarkViewRefresh) },
            onAppend = { onUpdate(BookmarkViewNextPage) },
            onUpdate = onUpdate
        )
    }
}
