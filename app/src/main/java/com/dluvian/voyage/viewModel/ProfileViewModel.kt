package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.ProfileViewCmd
import com.dluvian.voyage.model.ProfileViewNextPage
import com.dluvian.voyage.model.ProfileViewPopNprofile
import com.dluvian.voyage.model.ProfileViewPopPubkey
import com.dluvian.voyage.model.ProfileViewPushNprofile
import com.dluvian.voyage.model.ProfileViewPushPubkey
import com.dluvian.voyage.model.ProfileViewRefresh
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.provider.FeedProvider
import com.dluvian.voyage.provider.IEventUpdate
import rust.nostr.sdk.Event
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.PublicKey

class ProfileViewModel(
    val profileFeedState: LazyListState,
    val profileAboutState: LazyListState,
    val profileRelayState: LazyListState,
    val pagerState: PagerState,
    val feedProvider: FeedProvider,
    private val service: NostrService,
) : ViewModel(), IEventUpdate {
    val tabIndex = mutableIntStateOf(0)
    val pubkey = mutableStateOf<PublicKey?>(null)
    val isMe = mutableStateOf(false)
    val profile = mutableStateOf<Metadata?>(null)
    val nip65 = mutableStateOf<Event?>(null)
    val trustedBy = mutableStateOf<TrustProfile?>(null)
    val paginator = Paginator(feedProvider = feedProvider)

    fun handle(cmd: ProfileViewCmd) {
        when (cmd) {
            ProfileViewNextPage -> TODO()
            is ProfileViewPopNprofile -> TODO()
            is ProfileViewPopPubkey -> TODO()
            is ProfileViewPushNprofile -> TODO()
            is ProfileViewPushPubkey -> TODO()
            ProfileViewRefresh -> TODO()
        }
    }

    override suspend fun update(event: Event) {
        paginator.update(event)
        // TODO: update followstate, nip65 and trusedby
    }
}
