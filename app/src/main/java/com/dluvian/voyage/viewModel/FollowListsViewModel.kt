package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.SHORT_DELAY
import com.dluvian.voyage.TopicFollowState
import com.dluvian.voyage.model.FollowListsEventUpdate
import com.dluvian.voyage.model.FollowListsViewCmd
import com.dluvian.voyage.model.FollowListsViewRefresh
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.ShowFollowListsView
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.provider.NameProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Timestamp

class FollowListsViewModel(
    val contactListState: LazyListState,
    val topicListState: LazyListState,
    val pagerState: PagerState,
    private val service: NostrService,
    private val nameProvider: NameProvider
) : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
    val isRefreshing = mutableStateOf(false)
    val isEditing = mutableStateOf(false)
    val profiles = mutableStateOf(emptyList<TrustProfile>())
    val topics = mutableStateOf(emptyList<TopicFollowState>())

    fun handle(cmd: FollowListsViewCmd) {
        when (cmd) {
            ShowFollowListsView -> {
                viewModelScope.launch {
                    load(dbOnly = false)
                    sub()
                }
            }

            FollowListsViewRefresh -> {
                if (isRefreshing.value) return
                isRefreshing.value = true
                viewModelScope.launch {
                    sub()
                    delay(SHORT_DELAY)
                    nameProvider.reserve(pubkeys = profiles.value.map { it.pubkey }, dbOnly = false)
                }.invokeOnCompletion {
                    isRefreshing.value = false
                }
            }

            is FollowListsEventUpdate -> if (!isEditing.value) {
                when (cmd.event.kind().asStd()) {
                    // TODO: Issue: Update name even when editing
                    KindStandard.METADATA, KindStandard.CONTACT_LIST -> viewModelScope.launch {
                        loadProfiles(dbOnly = true)
                    }

                    KindStandard.INTERESTS -> viewModelScope.launch {
                        loadTopics()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private suspend fun sub() {
        val pubkey = service.pubkey()
        val kinds = listOf(KindStandard.CONTACT_LIST, KindStandard.INTERESTS)
            .map { Kind.fromStd(it) }
        val filter = Filter().author(pubkey).kinds(kinds).limit(2u)
        val events = service.dbQuery(filter)

        val profiles = events.firstOrNull { it.kind().asStd() == KindStandard.CONTACT_LIST }
        val topics = events.firstOrNull { it.kind().asStd() == KindStandard.INTERESTS }
        if (profiles == null && topics == null) {
            service.subscribe(filter)
            return
        }
        if (profiles != null) {
            val since = profiles.createdAt().asSecs() + 1u
            val profileFilter = Filter().author(pubkey)
                .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
                .since(Timestamp.fromSecs(since))
                .limit(1u)
            service.subscribe(profileFilter)
        }
        if (topics != null) {
            val since = topics.createdAt().asSecs() + 1u
            val topicFilter = Filter().author(pubkey)
                .kind(Kind.fromStd(KindStandard.INTERESTS))
                .since(Timestamp.fromSecs(since))
                .limit(1u)
            service.subscribe(topicFilter)
        }
    }

    private suspend fun load(dbOnly: Boolean) {
        loadTopics()
        loadProfiles(dbOnly)
    }

    private suspend fun loadProfiles(dbOnly: Boolean) {
        val pubkey = service.pubkey()
        val filter = Filter().author(pubkey).kind(Kind.fromStd(KindStandard.CONTACT_LIST)).limit(1u)
        val event = service.dbQuery(filter).firstOrNull()
        if (event == null) {
            profiles.value = emptyList()
            return
        }
        val pubkeys = event.tags().publicKeys().distinct()
        nameProvider.reserve(pubkeys, dbOnly)
        val names = nameProvider.names(pubkeys)
        val mapped = pubkeys.map { FollowedProfile(pubkey = it, name = names[it].orEmpty()) }
        profiles.value = mapped
    }

    private suspend fun loadTopics() {
        val pubkey = service.pubkey()
        val filter = Filter().author(pubkey).kind(Kind.fromStd(KindStandard.INTERESTS)).limit(1u)
        val event = service.dbQuery(filter).firstOrNull()
        if (event == null) {
            topics.value = emptyList()
            return
        }
        val mapped = event.tags().hashtags().distinct().map { Pair(it, true) }
        topics.value = mapped
    }
}
