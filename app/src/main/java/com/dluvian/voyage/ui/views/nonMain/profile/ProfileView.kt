package com.dluvian.voyage.ui.views.nonMain.profile

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.voyage.MAX_RELAYS
import com.dluvian.voyage.R
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.lightning
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.OpenNProfile
import com.dluvian.voyage.model.OpenRelayProfile
import com.dluvian.voyage.model.ProfileViewNextPage
import com.dluvian.voyage.model.ProfileViewRefresh
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.shortenNpub
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.SimpleTabPager
import com.dluvian.voyage.ui.components.icon.ClickableTrustIcon
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.text.AnnotatedTextWithHeader
import com.dluvian.voyage.ui.components.text.IndexedText
import com.dluvian.voyage.ui.components.text.SmallHeader
import com.dluvian.voyage.ui.theme.KeyIcon
import com.dluvian.voyage.ui.theme.LightningIcon
import com.dluvian.voyage.ui.theme.OpenIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.ProfileViewModel
import kotlinx.coroutines.launch
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.extractRelayList

@Composable
fun ProfileView(vm: ProfileViewModel, snackbar: SnackbarHostState, onUpdate: (Cmd) -> Unit) {
    val profile by vm.profile
    val meta by vm.meta
    val nip65 by vm.nip65
    val trustedBy by vm.trustedBy
    val headers = listOf(
        stringResource(id = R.string.posts),
        stringResource(id = R.string.about),
        stringResource(id = R.string.relays),
    )
    val scope = rememberCoroutineScope()

    profile?.let { profilerino ->
        ProfileScaffold(
            profile = profilerino,
            snackbar = snackbar,
            onUpdate = onUpdate
        ) {
            SimpleTabPager(
                headers = headers,
                index = vm.tabIndex,
                pagerState = vm.pagerState,
                onScrollUp = {
                    when (it) {
                        0 -> scope.launch { vm.profileFeedState.animateScrollToItem(0) }
                        1 -> scope.launch { vm.profileAboutState.animateScrollToItem(0) }
                        2 -> scope.launch { vm.profileRelayState.animateScrollToItem(0) }
                        else -> {}
                    }
                },
            ) {
                when (it) {
                    0 -> Feed(
                        paginator = vm.paginator,
                        state = vm.profileFeedState,
                        onRefresh = { onUpdate(ProfileViewRefresh) },
                        onAppend = { onUpdate(ProfileViewNextPage) },
                        onUpdate = onUpdate,
                    )

                    1 -> AboutPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = spacing.bigScreenEdge),
                        npub = remember(profilerino) { profilerino.pubkey.toBech32() },
                        nprofile = remember(profilerino, nip65) {
                            Nip19Profile(
                                profilerino.pubkey,
                                nip65?.let { extractRelayList(it).keys.take(MAX_RELAYS) }
                                    ?: emptyList()
                            ).toBech32()
                        },
                        lightning = meta.lightning(),
                        trustedBy = trustedBy,
                        about = AnnotatedString(meta.getAbout().orEmpty()),
                        isRefreshing = vm.paginator.isRefreshing.value,
                        state = vm.profileAboutState,
                        onUpdate = onUpdate
                    )

                    2 -> RelayPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = spacing.bigScreenEdge),
                        nip65Relays = remember(nip65) {
                            nip65?.let { extractRelayList(it) }
                                ?.toList()
                                .orEmpty()
                                .filter { (_, relayMeta) -> relayMeta == null }
                                .map { (url, _) -> url }
                        },
                        readOnlyRelays = remember(nip65) {
                            nip65?.let { extractRelayList(it) }
                                ?.toList()
                                .orEmpty()
                                .filter { (_, relayMeta) -> relayMeta == RelayMetadata.READ }
                                .map { (url, _) -> url }
                        },
                        writeOnlyRelays = remember(nip65) {
                            nip65?.let { extractRelayList(it) }
                                ?.toList()
                                .orEmpty()
                                .filter { (_, relayMeta) -> relayMeta == RelayMetadata.WRITE }
                                .map { (url, _) -> url }
                        },
                        isRefreshing = vm.paginator.isRefreshing.value,
                        state = vm.profileRelayState,
                        onUpdate = onUpdate
                    )

                    else -> ComingSoon()

                }
            }
        }
    }
}

@Composable
private fun AboutPage(
    npub: String,
    nprofile: String,
    lightning: String?,
    trustedBy: TrustProfile?,
    about: AnnotatedString?,
    isRefreshing: Boolean,
    state: LazyListState,
    modifier: Modifier = Modifier,
    onUpdate: (Cmd) -> Unit
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
                    shortenedText = shortenNpub(npub),
                    description = stringResource(id = R.string.npub)
                )
            }
            item {
                AboutPageTextRow(
                    modifier = Modifier.padding(vertical = spacing.medium),
                    icon = KeyIcon,
                    text = nprofile,
                    shortenedText = shortenNpub(npub),
                    description = stringResource(id = R.string.nprofile)
                )
            }
            if (!lightning.isNullOrEmpty()) item {
                AboutPageTextRow(
                    modifier = Modifier.padding(vertical = spacing.medium),
                    icon = LightningIcon,
                    text = lightning,
                    description = stringResource(id = R.string.lightning_address),
                    trailingIcon = {
                        // TODO: val launcher = getSimpleLauncher()
                        Icon(
                            modifier = Modifier
                                .padding(start = spacing.medium)
                                .size(sizing.smallIndicator)
                                .clickable {
                                    // TODO:
//                                    onUpdate(
//                                        OpenLightningWallet(
//                                            address = lightning,
//                                            launcher = launcher,
//                                            scope = scope,
//                                        )
//                                    )
                                },
                            imageVector = OpenIcon,
                            contentDescription = stringResource(id = R.string.open_lightning_address_in_wallet)
                        )
                    }
                )
            }
            if (trustedBy != null && trustedBy is FollowedProfile) item {
                Column(modifier = Modifier.padding(vertical = spacing.small)) {
                    SmallHeader(header = stringResource(id = R.string.semi_trusted_bc_you_follow))
                    ClickableTrustIcon(
                        profile = trustedBy,
                        onClick = {
                            onUpdate(OpenNProfile(Nip19Profile(trustedBy.pubkey)))
                        }
                    )
                }
            }
            if (!about.isNullOrEmpty()) item {
                AnnotatedTextWithHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.medium),
                    header = stringResource(id = R.string.about),
                    text = about
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
    description: String,
    trailingIcon: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val clip = LocalClipboard.current
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
            modifier = Modifier.weight(1f, fill = false),
            text = shortenedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailingIcon()
    }
}

@Composable
fun RelayPage(
    nip65Relays: List<RelayUrl>,
    readOnlyRelays: List<RelayUrl>,
    writeOnlyRelays: List<RelayUrl>,
    isRefreshing: Boolean,
    state: LazyListState,
    modifier: Modifier = Modifier,
    onUpdate: (Cmd) -> Unit,
) {
    ProfileViewPage(isRefreshing = isRefreshing, onUpdate = onUpdate) {
        if (nip65Relays.isEmpty() &&
            readOnlyRelays.isEmpty() &&
            writeOnlyRelays.isEmpty()
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
        }
    }
}

@Composable
private fun RelaySection(
    header: String,
    relays: List<RelayUrl>,
    onUpdate: (Cmd) -> Unit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileViewPage(
    isRefreshing: Boolean,
    onUpdate: (Cmd) -> Unit,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { onUpdate(ProfileViewRefresh) }) {
        content()
    }
}
