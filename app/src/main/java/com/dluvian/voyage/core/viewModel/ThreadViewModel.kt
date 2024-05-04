package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.ThreadViewAction
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.provider.ThreadProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.Nip19Event

class ThreadViewModel(
    private val threadProvider: ThreadProvider,
    private val threadCollapser: ThreadCollapser,
    val threadState: LazyListState,
) : ViewModel() {
    val isRefreshing = mutableStateOf(false)
    var parent: StateFlow<ParentUI?> = MutableStateFlow(null)
    val leveledReplies: MutableState<StateFlow<List<LeveledReplyUI>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    private val parentIds = mutableStateOf(emptySet<EventIdHex>())
    private var opPubkey: PubkeyHex? = null
    private var nevent: Nip19Event? = null

    fun openThread(nevent: Nip19Event, parentUi: ParentUI?) {
        val id = nevent.eventId().toHex()
        if (id == parent.value?.id) return

        leveledReplies.value = MutableStateFlow(emptyList())
        parentIds.value = setOf()
        opPubkey = nevent.author()?.toHex() ?: parentUi?.getRelevantPubkey()
        this.nevent = nevent

        parent = threadProvider
            .getParent(scope = viewModelScope, nevent = nevent)
            .onEach { if (opPubkey == null) opPubkey = it?.pubkey }
            .stateIn(viewModelScope, SharingStarted.Eagerly, parentUi)
        loadReplies(
            rootId = id,
            parentId = id,
            isInit = true,
            opPubkey = opPubkey
        )
    }

    fun handle(action: ThreadViewAction) {
        when (action) {
            is ThreadViewRefresh -> refresh()
            is ThreadViewToggleCollapse -> threadCollapser.toggleCollapse(id = action.id)
            is ThreadViewShowReplies -> loadReplies(
                rootId = parent.value?.getRelevantId(),
                parentId = action.id,
                isInit = false,
                opPubkey = opPubkey,
            )
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return

        val currentNevent = nevent ?: return
        val currentRoot = parent.value

        isRefreshing.value = true

        viewModelScope.launchIO {
            parent = threadProvider.getParent(scope = viewModelScope, nevent = currentNevent)
                .stateIn(viewModelScope, SharingStarted.Eagerly, currentRoot)
            leveledReplies.value = threadProvider.getLeveledReplies(
                rootId = currentNevent.eventId().toHex(),
                parentIds = parentIds.value,
                opPubkey = opPubkey,
            )
                .stateIn(
                    viewModelScope,
                    SharingStarted.Eagerly,
                    leveledReplies.value.value
                )
            delay(DELAY_1SEC)
        }.invokeOnCompletion { isRefreshing.value = false }
    }

    private fun loadReplies(
        rootId: EventIdHex?,
        parentId: EventIdHex,
        isInit: Boolean,
        opPubkey: PubkeyHex?,
    ) {
        if (rootId == null || parentIds.value.contains(parentId)) return

        val init = if (isInit) emptyList() else leveledReplies.value.value
        parentIds.value += parentId
        leveledReplies.value = threadProvider
            .getLeveledReplies(rootId = rootId, parentIds = parentIds.value, opPubkey = opPubkey)
            .stateIn(viewModelScope, SharingStarted.Eagerly, init)
    }
}
