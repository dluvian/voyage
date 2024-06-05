package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Bech32
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenRelayProfile
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.ProfileViewReplyAppend
import com.dluvian.voyage.core.ProfileViewRootAppend
import com.dluvian.voyage.core.copyAndToast
import com.dluvian.voyage.core.shortenBech32
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.PullRefreshBox
import com.dluvian.voyage.ui.components.SimplePager
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.components.text.IndexedText
import com.dluvian.voyage.ui.theme.KeyIcon
import com.dluvian.voyage.ui.theme.LightningIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileView(vm: ProfileViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val profile by vm.profile.value.collectAsState()
    val nip65Relays by vm.nip65Relays.value.collectAsState()
    val nip65RelayUrls = remember(nip65Relays) {
        nip65Relays.filter { it.isRead && it.isWrite }.map { it.url }
    }
    val readOnlyRelays = remember(nip65Relays) {
        nip65Relays.filter { it.isRead && !it.isWrite }.map { it.url }
    }
    val writeOnlyRelays = remember(nip65Relays) {
        nip65Relays.filter { it.isWrite && !it.isRead }.map { it.url }
    }
    val seenInRelays by vm.seenInRelays.value.collectAsState()
    val index = vm.tabIndex
    val isRefreshing by vm.rootPaginator.isRefreshing
    val headers = listOf(
        stringResource(id = R.string.posts),
        stringResource(id = R.string.replies),
        stringResource(id = R.string.about),
        stringResource(id = R.string.relays),
    )
    val scope = rememberCoroutineScope()

    ProfileScaffold(profile = profile, snackbar = snackbar, onUpdate = onUpdate) {
        SimplePager(
            headers = headers,
            index = index,
            pagerState = vm.pagerState,
            onScrollUp = {
                when (it) {
                    0 -> scope.launch { vm.rootFeedState.scrollToItem(0) }
                    1 -> scope.launch { vm.replyFeedState.scrollToItem(0) }
                    3 -> scope.launch { vm.profileAboutState.scrollToItem(0) }
                    4 -> scope.launch { vm.profileRelayState.scrollToItem(0) }
                    else -> {}
                }
            },
        ) {
            when (it) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = spacing.bigScreenEdge),
                    npub = profile.npub,
                    lightning = profile.lightning,
                    about = profile.about,
                    isRefreshing = isRefreshing,
                    state = vm.profileAboutState,
                    onUpdate = onUpdate
                )

                3 -> RelayPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = spacing.bigScreenEdge),
                    nip65Relays = nip65RelayUrls,
                    readOnlyRelays = readOnlyRelays,
                    writeOnlyRelays = writeOnlyRelays,
                    seenInRelays = seenInRelays,
                    isRefreshing = isRefreshing,
                    state = vm.profileRelayState,
                    onUpdate = onUpdate
                )

                else -> ComingSoon()

            }
        }
    }
}

@Composable
private fun AboutPage(
    npub: Bech32,
    lightning: String?,
    about: AnnotatedString?,
    isRefreshing: Boolean,
    state: LazyListState,
    modifier: Modifier = Modifier,
    onUpdate: OnUpdate
) {
    ProfileViewPage(isRefreshing = isRefreshing, onUpdate = onUpdate) {
        LazyColumn(modifier = modifier, state = state) {
            item {
                AboutPageTextRow(
                    modifier = Modifier
                        .padding(vertical = spacing.medium)
                        .padding(top = spacing.screenEdge),
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
            if (!about.isNullOrEmpty()) item {
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

@Composable
fun RelayPage(
    nip65Relays: List<RelayUrl>,
    readOnlyRelays: List<RelayUrl>,
    writeOnlyRelays: List<RelayUrl>,
    seenInRelays: List<RelayUrl>,
    isRefreshing: Boolean,
    state: LazyListState,
    modifier: Modifier = Modifier,
    onUpdate: OnUpdate,
) {
    ProfileViewPage(isRefreshing = isRefreshing, onUpdate = onUpdate) {
        if (nip65Relays.isEmpty() &&
            readOnlyRelays.isEmpty() &&
            writeOnlyRelays.isEmpty() &&
            seenInRelays.isEmpty()
        ) BaseHint(stringResource(id = R.string.no_relays_found))

        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = PaddingValues(top = spacing.screenEdge)
        ) {
            if (nip65Relays.isNotEmpty()) item {
                RelaySection(
                    header = stringResource(id = R.string.relay_list),
                    relays = nip65Relays,
                    onUpdate = onUpdate
                )
            }

            if (readOnlyRelays.isNotEmpty()) item {
                RelaySection(
                    header = stringResource(id = R.string.relay_list_read_only),
                    relays = readOnlyRelays,
                    onUpdate = onUpdate
                )
            }

            if (writeOnlyRelays.isNotEmpty()) item {
                RelaySection(
                    header = stringResource(id = R.string.relay_list_write_only),
                    relays = writeOnlyRelays,
                    onUpdate = onUpdate
                )
            }

            if (seenInRelays.isNotEmpty()) item {
                RelaySection(
                    header = stringResource(id = R.string.seen_in),
                    relays = seenInRelays,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
private fun RelaySection(
    header: String,
    relays: List<RelayUrl>,
    onUpdate: OnUpdate
) {
    Text(text = header, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(spacing.small))
    relays.forEachIndexed { i, relay ->
        IndexedText(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdate(OpenRelayProfile(relayUrl = relay)) },
            index = i + 1,
            text = relay,
            fontWeight = FontWeight.Normal
        )
    }
    Spacer(modifier = Modifier.height(spacing.xl))
}

@Composable
private fun ProfileViewPage(isRefreshing: Boolean, onUpdate: OnUpdate, content: ComposableContent) {
    PullRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ProfileViewRefresh) }) {
        content()
    }
}
