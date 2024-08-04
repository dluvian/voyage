package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.ProfileViewAction
import com.dluvian.voyage.core.ProfileViewLoadLists
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.ProfileViewReplyAppend
import com.dluvian.voyage.core.ProfileViewRootAppend
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.ItemSetProfile
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.MuteProvider
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
    muteProvider: MuteProvider,
    val rootFeedState: LazyListState,
    val replyFeedState: LazyListState,
    val profileAboutState: LazyListState,
    val profileRelayState: LazyListState,
    val pagerState: PagerState,
    private val subCreator: SubscriptionCreator,
    private val profileProvider: ProfileProvider,
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
    private val itemSetProvider: ItemSetProvider,
) : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
    val addableLists = mutableStateOf(emptyList<ItemSetMeta>())
    val nonAddableLists = mutableStateOf(emptyList<ItemSetMeta>())
    val profile: MutableState<StateFlow<FullProfileUI>> =
        mutableStateOf(MutableStateFlow(FullProfileUI()))
    val nip65Relays: MutableState<StateFlow<List<Nip65Relay>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val seenInRelays: MutableState<StateFlow<List<RelayUrl>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val trustedBy: MutableState<StateFlow<AdvancedProfileView?>> =
        mutableStateOf(MutableStateFlow(null))
    val rootPaginator = Paginator(
        feedProvider = feedProvider,
        muteProvider = muteProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )
    val replyPaginator = Paginator(
        feedProvider = feedProvider,
        muteProvider = muteProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    @OptIn(ExperimentalFoundationApi::class)
    fun openProfile(profileNavView: ProfileNavView) {
        val pubkeyHex = profileNavView.nprofile.publicKey().toHex()
        if (profile.value.value.inner.pubkey == pubkeyHex) return

        // TODO: Sub contactlist if friend and force refreshing

        subCreator.unsubAll()
        profile.value = profileProvider
            .getProfileFlow(nprofile = profileNavView.nprofile, subProfile = true)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FullProfileUI(inner = AdvancedProfileView(pubkey = pubkeyHex))
            )
        tabIndex.intValue = 0
        viewModelScope.launch {
            pagerState.scrollToPage(0)
        }
        rootPaginator.reinit(setting = ProfileRootFeedSetting(nprofile = profileNavView.nprofile))
        replyPaginator.reinit(setting = ReplyFeedSetting(nprofile = profileNavView.nprofile))
        trustedBy.value = profileProvider.getTrustedByFlow(pubkey = pubkeyHex)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
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
            ProfileViewLoadLists -> updateLists(pubkey = profile.value.value.inner.pubkey)
        }
    }

    private fun refresh() {
        subCreator.unsubAll()
        val nprofile = createNprofile(hex = profile.value.value.inner.pubkey)
        profile.value = profileProvider.getProfileFlow(nprofile = nprofile, subProfile = false)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FullProfileUI(inner = profile.value.value.inner)
            )
        rootPaginator.refresh()
        replyPaginator.refresh()
    }

    private fun updateLists(pubkey: PubkeyHex) {
        viewModelScope.launchIO {
            addableLists.value = itemSetProvider
                .getAddableSets(item = ItemSetProfile(pubkey = pubkey))
            nonAddableLists.value = itemSetProvider
                .getNonAddableSets(item = ItemSetProfile(pubkey = pubkey))
        }
    }
}
