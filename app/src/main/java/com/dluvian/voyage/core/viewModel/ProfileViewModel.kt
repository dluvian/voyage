package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.core.ProfileViewAction
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.ProfileViewReplyAppend
import com.dluvian.voyage.core.ProfileViewRootAppend
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.toBech32
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel @OptIn(ExperimentalFoundationApi::class) constructor(
    feedProvider: FeedProvider,
    private val subCreator: SubscriptionCreator,
    private val profileProvider: ProfileProvider,
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
    val rootFeedState: LazyListState,
    val replyFeedState: LazyListState,
    val pagerState: PagerState,
) : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
    val profile: MutableState<StateFlow<FullProfileUI>> =
        mutableStateOf(MutableStateFlow(FullProfileUI()))
    val nip65Relays: MutableState<StateFlow<List<Nip65Relay>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val seenInRelays: MutableState<StateFlow<List<RelayUrl>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val rootPaginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )
    val replyPaginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    @OptIn(ExperimentalFoundationApi::class)
    fun openProfile(profileNavView: ProfileNavView) {
        val pubkeyHex = profileNavView.nprofile.publicKey().toHex()
        if (profile.value.value.inner.pubkey == pubkeyHex) return

        subCreator.unsubAll()
        profile.value = profileProvider.getProfileFlow(nprofile = profileNavView.nprofile)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FullProfileUI(
                    inner = AdvancedProfileView(pubkey = pubkeyHex),
                    npub = pubkeyHex.toBech32()
                )
            )
        tabIndex.intValue = 0
        viewModelScope.launch { pagerState.scrollToPage(0) }
        rootPaginator.init(setting = ProfileRootFeedSetting(pubkey = pubkeyHex))
        replyPaginator.init(setting = ReplyFeedSetting(pubkey = pubkeyHex))
        nip65Relays.value = nip65Dao.getNip65Flow(pubkey = pubkeyHex)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        seenInRelays.value = eventRelayDao.getEventRelays(pubkey = pubkeyHex)
            .map { it.filter { relay -> relay.isNotEmpty() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    fun handle(action: ProfileViewAction) {
        when (action) {
            ProfileViewRefresh -> refresh()
            ProfileViewRootAppend -> rootPaginator.append()
            ProfileViewReplyAppend -> replyPaginator.append()
        }
    }

    private fun refresh() {
        subCreator.unsubAll()
        val nprofile = createNprofile(hex = profile.value.value.inner.pubkey)
        profile.value = profileProvider.getProfileFlow(nprofile = nprofile)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FullProfileUI(inner = profile.value.value.inner, npub = profile.value.value.npub)
            )
        rootPaginator.refresh()
        replyPaginator.refresh()
    }
}
