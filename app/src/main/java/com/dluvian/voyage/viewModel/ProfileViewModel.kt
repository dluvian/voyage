package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.cmd.ProfileViewAction
import com.dluvian.voyage.cmd.ProfileViewLoadLists
import com.dluvian.voyage.cmd.ProfileViewRefresh
import com.dluvian.voyage.cmd.ProfileViewReplyAppend
import com.dluvian.voyage.cmd.ProfileViewRootAppend
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.ItemSetProfile
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.filterSetting.FullProfileUI
import com.dluvian.voyage.filterSetting.ItemSetMeta
import com.dluvian.voyage.filterSetting.ProfileFeedSetting
import com.dluvian.voyage.filterSetting.ReplyFeedSetting
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.provider.IEventUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rust.nostr.sdk.Event
import rust.nostr.sdk.Nip19Profile

class ProfileViewModel(
    feedProvider: FeedProvider,
    val rootFeedState: LazyListState,
    val replyFeedState: LazyListState,
    val profileAboutState: LazyListState,
    val profileRelayState: LazyListState,
    val pagerState: PagerState,
    private val nostrSubscriber: NostrSubscriber,
    private val profileProvider: ProfileProvider,
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
    private val itemSetProvider: ItemSetProvider,
    private val myPubkeyProvider: IMyPubkeyProvider,
) : ViewModel(), IEventUpdate {
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
        scope = viewModelScope,
        subCreator = nostrSubscriber.subCreator
    )

    fun openNProfile(nprofile: Nip19Profile) {
        val profileEvent = TODO()
        openProfile(profileEvent)
    }

    fun openProfile(profile: Event) {
        val pubkeyHex = profileNavView.nprofile.publicKey().toHex()
        if (profile.value.value.inner.pubkey == pubkeyHex) return

        nostrSubscriber.subCreator.unsubAll()
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
            trustedBy.value = if (pubkeyHex != myPubkeyProvider.getPubkeyHex()) {
                profileProvider.getTrustedByFlow(pubkey = pubkeyHex)
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
            } else {
                MutableStateFlow(null)
            }
        }
        rootPaginator.reinit(setting = ProfileFeedSetting(nprofile = profileNavView.nprofile))
        replyPaginator.reinit(setting = ReplyFeedSetting(nprofile = profileNavView.nprofile))
        nip65Relays.value = nip65Dao.getNip65Flow(pubkey = pubkeyHex)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        seenInRelays.value = eventRelayDao.getEventRelays(pubkey = pubkeyHex)
            .map { it.filter { relay -> relay.isNotEmpty() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    fun handle(action: ProfileViewAction) {
        when (action) {
            ProfileViewRefresh -> refresh()
            ProfileViewRootAppend -> rootPaginator.nextPage()
            ProfileViewReplyAppend -> replyPaginator.append()
            ProfileViewLoadLists -> updateLists(pubkey = profile.value.value.inner.pubkey)
        }
    }

    private fun refresh() {
        nostrSubscriber.subCreator.unsubAll()
        val nprofile = createNprofile(hex = profile.value.value.inner.pubkey)
        profile.value = profileProvider.getProfileFlow(nprofile = nprofile, subProfile = false)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FullProfileUI(inner = profile.value.value.inner)
            )

        // Sub contacts to update trustedBy of a trusted profile
        if (profile.value.value.inner.isFriend) {
            viewModelScope.launchIO {
                nostrSubscriber.subContactList(nprofile = nprofile)
            }
        }

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
