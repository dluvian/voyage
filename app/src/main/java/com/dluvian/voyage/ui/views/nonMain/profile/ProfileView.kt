package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Bech32
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.ProfileViewReplyAppend
import com.dluvian.voyage.core.ProfileViewRootAppend
import com.dluvian.voyage.core.copyAndToast
import com.dluvian.voyage.core.shortenBech32
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.KeyIcon
import com.dluvian.voyage.ui.theme.LightningIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileView(vm: ProfileViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val profile by vm.profile.value.collectAsState()
    val index = remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    val isRefreshing by vm.rootPaginator.isRefreshing

    LaunchedEffect(key1 = pagerState.currentPage) {
        index.intValue = pagerState.currentPage
    }

    ProfileScaffold(profile = profile, snackbar = snackbar, onUpdate = onUpdate) {
        Column {
            ProfileTabRow(
                index = index,
                onClickPage = { i -> scope.launch { pagerState.animateScrollToPage(i) } })
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = pagerState
            ) { page ->
                when (page) {
                    0 -> Feed(
                        paginator = vm.rootPaginator,
                        state = vm.rootFeedState,
                        onRefresh = { onUpdate(ProfileViewRefresh) },
                        onAppend = { onUpdate(ProfileViewRootAppend) },
                        onUpdate = onUpdate,
                    )

                    1 -> Feed(
                        paginator = vm.replyPaginator,
                        state = vm.replyFeedState,
                        onRefresh = { onUpdate(ProfileViewRefresh) },
                        onAppend = { onUpdate(ProfileViewReplyAppend) },
                        onUpdate = onUpdate,
                    )

                    2 -> AboutPage(
                        isRefreshing = isRefreshing,
                        npub = profile.npub,
                        lightning = profile.lightning,
                        about = profile.about,
                        onUpdate = onUpdate
                    )

                    3 -> ComingSoon() // Relays

                    else -> ComingSoon()

                }
            }
        }
    }
}

@Composable
private fun AboutPage(
    isRefreshing: Boolean,
    npub: Bech32,
    lightning: String?,
    about: AnnotatedString?,
    onUpdate: OnUpdate
) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ProfileViewRefresh) }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.screenEdge)
        ) {
            item {
                AboutPageTextRow(
                    modifier = Modifier.padding(vertical = spacing.medium),
                    icon = KeyIcon,
                    text = npub,
                    shortenedText = npub.shortenBech32(),
                    description = stringResource(id = R.string.profile_identifier)
                )
            }
            if (!lightning.isNullOrEmpty()) item {
                AboutPageTextRow(
                    modifier = Modifier.padding(vertical = spacing.medium),
                    icon = LightningIcon,
                    text = lightning,
                    description = stringResource(id = R.string.lightning_address)
                )
            }
            if (about != null) item {
                AboutSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.medium),
                    about = about,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
private fun AboutPageTextRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    shortenedText: String = text,
    description: String
) {
    val context = LocalContext.current
    val clip = LocalClipboardManager.current
    val toast = stringResource(id = R.string.value_copied)

    Row(
        modifier = modifier.clickable {
            copyAndToast(text = text, toast = toast, context = context, clip = clip)
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(sizing.smallIndicator),
            imageVector = icon,
            contentDescription = description
        )
        Spacer(modifier = Modifier.width(spacing.small))
        Text(
            text = shortenedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AboutSection(
    modifier: Modifier = Modifier,
    about: AnnotatedString,
    onUpdate: OnUpdate
) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.about),
            style = MaterialTheme.typography.titleMedium
        )
        AnnotatedText(
            text = about,
            onClick = { offset ->
                onUpdate(ClickText(text = about, offset = offset, uriHandler = uriHandler))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTabRow(index: MutableIntState, onClickPage: (Int) -> Unit) {
    // Set higher zIndex to hide resting refresh indicator
    PrimaryTabRow(modifier = Modifier.zIndex(2f), selectedTabIndex = index.intValue) {
        Tab(
            selected = index.intValue == 0,
            onClick = {
                index.intValue = 0
                onClickPage(0)
            },
            text = { Text(stringResource(id = R.string.posts)) })
        Tab(
            selected = index.intValue == 1,
            onClick = {
                index.intValue = 1
                onClickPage(1)
            },
            text = { Text(stringResource(id = R.string.replies)) })
        Tab(
            selected = index.intValue == 2,
            onClick = {
                index.intValue = 2
                onClickPage(2)
            },
            text = { Text(stringResource(id = R.string.about)) })
        Tab(
            selected = index.intValue == 3,
            onClick = {
                index.intValue = 3
                onClickPage(3)
            },
            text = { Text(stringResource(id = R.string.relays)) })
    }
}
